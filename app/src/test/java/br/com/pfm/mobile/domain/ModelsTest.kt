package br.com.pfm.mobile.domain

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ModelsTest {

    @Test
    fun idempotencyKeyIsDeterministicSha256Hex() {
        val a = PendingReceiptScan.computeDeterministicIdempotencyKey("t", "http://x", "pix")
        val b = PendingReceiptScan.computeDeterministicIdempotencyKey(" t ", " http://x ", " PIX ")
        assertEquals(a, b)
        assertEquals(64, a.length)
        assertTrue(a.all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun idempotencyKeyDiffersForDifferentInput() {
        val a = PendingReceiptScan.computeDeterministicIdempotencyKey("t", "u1", "PIX")
        val b = PendingReceiptScan.computeDeterministicIdempotencyKey("t", "u2", "PIX")
        assertNotEquals(a, b)
    }

    @Test
    fun pendingScanDefaults() {
        val s = PendingReceiptScan("1", "t", "u", "PIX", null, null, "k", SyncStatus.LOCAL_PENDING)
        assertEquals(0, s.attemptCount)
        assertTrue(s.inflationNotices.isEmpty())
        assertNull(s.estimatedAmount)
        assertTrue(s.capturedAtEpochMillis > 0)
    }

    @Test
    fun inflationNoticeKeepsBigDecimal() {
        val n = EanPriceInflationNotice("789", "Leite", BigDecimal("4.00"), BigDecimal("5.00"), BigDecimal("25.00"))
        assertEquals(BigDecimal("25.00"), n.inflationPercentage)
        assertEquals(SyncStatus.RECONCILED, SyncStatus.valueOf("RECONCILED"))
    }

    @Test
    fun quickExpenseAcceptsPositiveAmount() {
        val e = QuickExpense("1", "t", "Café", "FOOD", BigDecimal("0.01"), "PIX")
        assertEquals(SyncStatus.LOCAL_PENDING, e.syncStatus)
        assertEquals(BigDecimal("0.01"), e.amount)
    }

    @Test
    fun quickExpenseRejectsZeroAndNegative() {
        for (v in listOf(BigDecimal.ZERO, BigDecimal("-1"), BigDecimal("0.00001"))) {
            try {
                QuickExpense("1", "t", "d", "c", v, "PIX")
                fail("expected IllegalArgumentException for $v")
            } catch (_: IllegalArgumentException) {
            }
        }
    }
}
