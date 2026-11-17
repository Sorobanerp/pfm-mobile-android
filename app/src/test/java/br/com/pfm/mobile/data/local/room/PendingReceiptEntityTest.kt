package br.com.pfm.mobile.data.local.room

import br.com.pfm.mobile.domain.PendingReceiptScan
import br.com.pfm.mobile.domain.SyncStatus
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PendingReceiptEntityTest {

    @Test
    fun roundTripPreservesBigDecimalAndFields() {
        val scan = PendingReceiptScan(
            id = "1", tenantId = "t", qrCodeUrl = "u", paymentMethod = "PIX", cardId = "c",
            estimatedAmount = BigDecimal("123.4500"), idempotencyKey = "k",
            syncStatus = SyncStatus.RETRY_SCHEDULED, attemptCount = 3, capturedAtEpochMillis = 42L
        )
        val entity = PendingReceiptEntity.fromDomain(scan)
        assertEquals("123.4500", entity.estimatedAmount)
        assertEquals("RETRY_SCHEDULED", entity.syncStatus)
        assertEquals(scan, entity.toDomain())
    }

    @Test
    fun nullAmountAndCardMapToNull() {
        val scan = PendingReceiptScan("1", "t", "u", "PIX", null, null, "k", SyncStatus.SYNCED, 0, 1L)
        val back = PendingReceiptEntity.fromDomain(scan)
        assertNull(back.estimatedAmount)
        assertNull(back.cardId)
        assertNull(back.toDomain().estimatedAmount)
    }
}
