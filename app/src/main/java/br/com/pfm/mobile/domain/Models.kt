package br.com.pfm.mobile.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant

/**
 * Clean Architecture Pure Domain Models using [BigDecimal] (scale 4, HALF_EVEN) for monetary precision.
 */
enum class SyncStatus {
    LOCAL_PENDING,
    SYNCED,
    RECONCILED,
    RETRY_SCHEDULED
}

data class EanPriceInflationNotice(
    val ean: String,
    val description: String,
    val previousUnitPrice: BigDecimal,
    val currentUnitPrice: BigDecimal,
    val inflationPercentage: BigDecimal
)

data class PendingReceiptScan(
    val id: String,
    val tenantId: String,
    val qrCodeUrl: String,
    val paymentMethod: String,
    val cardId: String?,
    val estimatedAmount: BigDecimal?,
    val idempotencyKey: String,
    val syncStatus: SyncStatus,
    val attemptCount: Int = 0,
    val capturedAtEpochMillis: Long = Instant.now().toEpochMilli(),
    val inflationNotices: List<EanPriceInflationNotice> = emptyList()
) {
    companion object {
        fun computeDeterministicIdempotencyKey(
            tenantId: String,
            qrCodeUrl: String,
            paymentMethod: String
        ): String {
            val canonical = "${tenantId.trim()}|${qrCodeUrl.trim()}|${paymentMethod.trim().uppercase()}"
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(canonical.toByteArray(StandardCharsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}

data class QuickExpense(
    val id: String,
    val tenantId: String,
    val description: String,
    val categoryCode: String,
    val amount: BigDecimal,
    val paymentMethod: String,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_PENDING
) {
    init {
        require(amount.setScale(4, RoundingMode.HALF_EVEN) > BigDecimal.ZERO) {
            "QuickExpense amount must be strictly positive"
        }
    }
}
