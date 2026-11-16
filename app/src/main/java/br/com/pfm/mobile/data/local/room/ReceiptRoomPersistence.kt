package br.com.pfm.mobile.data.local.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import br.com.pfm.mobile.domain.PendingReceiptScan
import br.com.pfm.mobile.domain.SyncStatus
import java.math.BigDecimal

/**
 * Room Entity persisting SEFAZ NFC-e QR Code URLs locally in < 100ms (100% Offline-First, even in airplane mode).
 */
@Entity(tableName = "pending_receipt_scans")
data class PendingReceiptEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "tenant_id")
    val tenantId: String,

    @ColumnInfo(name = "qr_code_url")
    val qrCodeUrl: String,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,

    @ColumnInfo(name = "card_id")
    val cardId: String?,

    @ColumnInfo(name = "estimated_amount")
    val estimatedAmount: String?,

    @ColumnInfo(name = "idempotency_key")
    val idempotencyKey: String,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String,

    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int,

    @ColumnInfo(name = "captured_at_epoch_ms")
    val capturedAtEpochMillis: Long
) {
    fun toDomain(): PendingReceiptScan = PendingReceiptScan(
        id = id,
        tenantId = tenantId,
        qrCodeUrl = qrCodeUrl,
        paymentMethod = paymentMethod,
        cardId = cardId,
        estimatedAmount = estimatedAmount?.let { BigDecimal(it) },
        idempotencyKey = idempotencyKey,
        syncStatus = SyncStatus.valueOf(syncStatus),
        attemptCount = attemptCount,
        capturedAtEpochMillis = capturedAtEpochMillis
    )

    companion object {
        fun fromDomain(domain: PendingReceiptScan): PendingReceiptEntity = PendingReceiptEntity(
            id = domain.id,
            tenantId = domain.tenantId,
            qrCodeUrl = domain.qrCodeUrl,
            paymentMethod = domain.paymentMethod,
            cardId = domain.cardId,
            estimatedAmount = domain.estimatedAmount?.toPlainString(),
            idempotencyKey = domain.idempotencyKey,
            syncStatus = domain.syncStatus.name,
            attemptCount = domain.attemptCount,
            capturedAtEpochMillis = domain.capturedAtEpochMillis
        )
    }
}

@Dao
interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingReceipt(entity: PendingReceiptEntity)

    @Query("SELECT * FROM pending_receipt_scans WHERE sync_status IN ('LOCAL_PENDING', 'RETRY_SCHEDULED') ORDER BY captured_at_epoch_ms ASC")
    suspend fun findUnsyncedReceipts(): List<PendingReceiptEntity>

    @Query("SELECT * FROM pending_receipt_scans ORDER BY captured_at_epoch_ms DESC")
    suspend fun findAllReceipts(): List<PendingReceiptEntity>

    @Query("UPDATE pending_receipt_scans SET sync_status = :newStatus, attempt_count = :attemptCount WHERE id = :id")
    suspend fun updateSyncStatus(id: String, newStatus: String, attemptCount: Int)
}
