package br.com.pfm.mobile.data.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import br.com.pfm.mobile.data.local.room.PendingReceiptEntity
import br.com.pfm.mobile.data.local.room.ReceiptDao
import br.com.pfm.mobile.data.remote.TraceparentInterceptor
import br.com.pfm.mobile.domain.PendingReceiptScan
import br.com.pfm.mobile.domain.SyncStatus
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import okhttp3.OkHttpClient

/**
 * Gateway contract for posting pending NFC-e QR Code URLs to `POST /api/v1/receipts/nfce`
 * over HTTP/2 & HTTP/3 QUIC (OkHttp + Cronet).
 */
fun interface NfceRemoteIngestionGateway {
    suspend fun uploadNfceQrCode(
        tenantId: String,
        qrCodeUrl: String,
        cardId: String?,
        paymentMethod: String,
        idempotencyKey: String
    ): RemoteReceiptSyncResponse
}

data class RemoteReceiptSyncResponse(
    val reconciled: Boolean,
    val receiptId: String
)

enum class SyncBatchOutcome {
    SUCCESS,
    RETRY_EXPONENTIAL
}

/**
 * Offline-First Coordinator and WorkManager Sync Engine (`NetworkType.CONNECTED` + `BackoffPolicy.EXPONENTIAL`).
 * Persists scanned NFC-e QR Codes to local Room storage in < 100ms (even in airplane mode)
 * and syncs them asynchronously to `POST /api/v1/receipts/nfce` with deterministic SHA-256 `idempotency_key`.
 */
class ReceiptSyncWorkerCoordinator(
    private val receiptDao: ReceiptDao,
    private val remoteGateway: NfceRemoteIngestionGateway
) {

    /**
     * Captures and saves a scanned NFC-e QR Code offline in < 100ms.
     */
    suspend fun captureQrCodeOffline(
        tenantId: String,
        qrCodeUrl: String,
        paymentMethod: String,
        cardId: String? = null
    ): Pair<PendingReceiptScan, Long> {
        lateinit var scan: PendingReceiptScan
        val elapsedMs = measureTimeMillis {
            val idempotencyKey = PendingReceiptScan.computeDeterministicIdempotencyKey(
                tenantId = tenantId,
                qrCodeUrl = qrCodeUrl,
                paymentMethod = paymentMethod
            )
            scan = PendingReceiptScan(
                id = UUID.randomUUID().toString(),
                tenantId = tenantId,
                qrCodeUrl = qrCodeUrl,
                paymentMethod = paymentMethod,
                cardId = cardId,
                estimatedAmount = null,
                idempotencyKey = idempotencyKey,
                syncStatus = SyncStatus.LOCAL_PENDING
            )
            receiptDao.insertPendingReceipt(PendingReceiptEntity.fromDomain(scan))
        }
        return scan to elapsedMs
    }

    /**
     * Executes the synchronization batch invoked by WorkManager when `NetworkType.CONNECTED` is satisfied.
     * Returns [SyncBatchOutcome.RETRY_EXPONENTIAL] if any receipt fails due to transient network/server errors.
     */
    suspend fun syncPendingReceipts(): SyncBatchOutcome {
        val unsynced = receiptDao.findUnsyncedReceipts()
        var requiresRetry = false

        for (entity in unsynced) {
            try {
                val response = remoteGateway.uploadNfceQrCode(
                    tenantId = entity.tenantId,
                    qrCodeUrl = entity.qrCodeUrl,
                    cardId = entity.cardId,
                    paymentMethod = entity.paymentMethod,
                    idempotencyKey = entity.idempotencyKey
                )
                val finalStatus = if (response.reconciled) {
                    SyncStatus.RECONCILED
                } else {
                    SyncStatus.SYNCED
                }
                receiptDao.updateSyncStatus(
                    id = entity.id,
                    newStatus = finalStatus.name,
                    attemptCount = entity.attemptCount + 1
                )
            } catch (_: Exception) {
                requiresRetry = true
                receiptDao.updateSyncStatus(
                    id = entity.id,
                    newStatus = SyncStatus.RETRY_SCHEDULED.name,
                    attemptCount = entity.attemptCount + 1
                )
            }
        }

        return if (requiresRetry) SyncBatchOutcome.RETRY_EXPONENTIAL else SyncBatchOutcome.SUCCESS
    }

    companion object {
        const val WORK_NAME = "pfm_offline_nfce_receipt_sync"
        const val INITIAL_BACKOFF_SECONDS = 15L

        /**
         * Builds the WorkManager [OneTimeWorkRequest] enforcing `NetworkType.CONNECTED`
         * and `BackoffPolicy.EXPONENTIAL`.
         */
        fun buildWorkRequestDescriptor(): Map<String, Any> = mapOf(
            "workName" to WORK_NAME,
            "networkType" to NetworkType.CONNECTED.name,
            "backoffPolicy" to BackoffPolicy.EXPONENTIAL.name,
            "initialBackoffSeconds" to INITIAL_BACKOFF_SECONDS
        )

        fun buildConstraints(): Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        inline fun <reified W : androidx.work.ListenableWorker> buildWorkRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<W>()
                .setConstraints(buildConstraints())
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    INITIAL_BACKOFF_SECONDS,
                    TimeUnit.SECONDS
                )
                .build()

        fun buildSyncHttpClient(): OkHttpClient =
            TraceparentInterceptor.buildTracedHttpClient()
    }
}
