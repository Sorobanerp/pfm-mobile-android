package br.com.pfm.mobile.data.sync

import androidx.work.BackoffPolicy
import androidx.work.NetworkType
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import br.com.pfm.mobile.domain.SyncStatus
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptSyncCoordinatorExtraTest {

    private val tenant = "tenant-1"

    @Test
    fun syncWithNothingPendingIsSuccess() = runTest {
        val c = ReceiptSyncWorkerCoordinator(InMemoryReceiptDao()) { _, _, _, _, _ ->
            error("gateway must not be called")
        }
        assertEquals(SyncBatchOutcome.SUCCESS, c.syncPendingReceipts())
    }

    @Test
    fun nonReconciledResponseMarksSynced() = runTest {
        val dao = InMemoryReceiptDao()
        val c = ReceiptSyncWorkerCoordinator(dao) { _, _, _, _, _ ->
            RemoteReceiptSyncResponse(reconciled = false, receiptId = "r")
        }
        c.captureQrCodeOffline(tenant, "http://q", "PIX")
        assertEquals(SyncBatchOutcome.SUCCESS, c.syncPendingReceipts())
        val e = dao.findAllReceipts().single()
        assertEquals(SyncStatus.SYNCED.name, e.syncStatus)
        assertEquals(1, e.attemptCount)
        // synced items are not re-sent
        assertTrue(dao.findUnsyncedReceipts().isEmpty())
    }

    @Test
    fun partialFailureStillRequestsRetryAndForwardsFields() = runTest {
        val dao = InMemoryReceiptDao()
        val seen = mutableListOf<String>()
        val c = ReceiptSyncWorkerCoordinator(dao) { t, url, card, pm, key ->
            seen += "$t|$url|$card|$pm|${key.length}"
            if (url == "http://bad") throw IOException("503")
            RemoteReceiptSyncResponse(true, "r")
        }
        c.captureQrCodeOffline(tenant, "http://ok", "PIX", "card-1")
        c.captureQrCodeOffline(tenant, "http://bad", "PIX")
        assertEquals(SyncBatchOutcome.RETRY_EXPONENTIAL, c.syncPendingReceipts())
        assertTrue(seen.contains("tenant-1|http://ok|card-1|PIX|64"))
        val byUrl = dao.findAllReceipts().associateBy { it.qrCodeUrl }
        assertEquals("RECONCILED", byUrl.getValue("http://ok").syncStatus)
        assertEquals("RETRY_SCHEDULED", byUrl.getValue("http://bad").syncStatus)
    }

    @Test
    fun captureProducesDeterministicKeyAndLocalPending() = runTest {
        val dao = InMemoryReceiptDao()
        val c = ReceiptSyncWorkerCoordinator(dao) { _, _, _, _, _ -> error("unused") }
        val (a, _) = c.captureQrCodeOffline(tenant, "http://q", "PIX")
        val (b, _) = c.captureQrCodeOffline(tenant, "http://q", "pix")
        assertEquals(a.idempotencyKey, b.idempotencyKey)
        assertEquals(SyncStatus.LOCAL_PENDING, a.syncStatus)
        assertNotNull(a.id)
    }

    @Test
    fun descriptorDescribesExponentialConnectedWork() {
        val d = ReceiptSyncWorkerCoordinator.buildWorkRequestDescriptor()
        assertEquals(ReceiptSyncWorkerCoordinator.WORK_NAME, d["workName"])
        assertEquals(NetworkType.CONNECTED.name, d["networkType"])
        assertEquals(BackoffPolicy.EXPONENTIAL.name, d["backoffPolicy"])
        assertEquals(15L, d["initialBackoffSeconds"])
    }

    @Test
    fun constraintsRequireConnectedNetwork() {
        assertEquals(
            NetworkType.CONNECTED,
            ReceiptSyncWorkerCoordinator.buildConstraints().requiredNetworkType
        )
    }

    abstract class DummyWorker(c: android.content.Context, p: WorkerParameters) : Worker(c, p)

    @Test
    fun workRequestCarriesConstraintsAndBackoff() {
        val req = ReceiptSyncWorkerCoordinator.buildWorkRequest<DummyWorker>()
        assertEquals(NetworkType.CONNECTED, req.workSpec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, req.workSpec.backoffPolicy)
    }
}
