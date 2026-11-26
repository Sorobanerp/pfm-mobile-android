package br.com.pfm.mobile

import br.com.pfm.mobile.domain.FamilyMemberSummary
import br.com.pfm.mobile.domain.FamilyRole
import br.com.pfm.mobile.domain.PersonalInflationEvaluator
import br.com.pfm.mobile.ui.theme.OrganicPulseColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class FamilyProfileAndInflationTest {

    @Test
    fun `exposes shared Organic Financial Pulse color tokens`() {
        assertEquals(androidx.compose.ui.graphics.Color(0xFF8CBA51), OrganicPulseColors.Primary)
        assertTrue(OrganicPulseColors.LimeGlow != OrganicPulseColors.CoralCritical)
        assertTrue(OrganicPulseColors.WarmPeach != OrganicPulseColors.Canvas)
        assertTrue(OrganicPulseColors.Surface != OrganicPulseColors.Border)
        assertTrue(OrganicPulseColors.DeepText != OrganicPulseColors.MutedText)
        assertTrue(OrganicPulseColors.WarmPeachText != OrganicPulseColors.Primary)
    }

    @Test
    fun `triggers personal inflation alert when EAN unit price increases above threshold`() {
        val alert = PersonalInflationEvaluator.evaluate(
            ean = "7891000100103",
            itemDescription = "Café Torrado 500g",
            previousPrice = BigDecimal("18.00"),
            currentPrice = BigDecimal("21.60")
        )

        assertEquals("7891000100103", alert.ean)
        assertEquals("Café Torrado 500g", alert.itemDescription)
        assertEquals(BigDecimal("20.00"), alert.deltaPercentage)
        assertTrue(alert.isAlertTriggered)
    }

    @Test
    fun `does not trigger alert when price variation stays below threshold`() {
        val alert = PersonalInflationEvaluator.evaluate(
            ean = "7891000200201",
            itemDescription = "Arroz Integral 1kg",
            previousPrice = BigDecimal("10.00"),
            currentPrice = BigDecimal("10.20")
        )

        assertEquals(BigDecimal("2.00"), alert.deltaPercentage)
        assertFalse(alert.isAlertTriggered)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects non-positive previousPrice`() {
        PersonalInflationEvaluator.evaluate(
            ean = "000",
            itemDescription = "Item",
            previousPrice = BigDecimal.ZERO,
            currentPrice = BigDecimal("5.00")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects non-positive currentPrice`() {
        PersonalInflationEvaluator.evaluate(
            ean = "000",
            itemDescription = "Item",
            previousPrice = BigDecimal("5.00"),
            currentPrice = BigDecimal.ZERO
        )
    }

    @Test
    fun `stores family member RBAC roles and monthly allowance`() {
        val owner = FamilyMemberSummary(
            id = "m-1",
            displayName = "Titular",
            email = "titular@familia.local",
            role = FamilyRole.OWNER,
            monthlyAllowance = BigDecimal("5000.00")
        )
        assertEquals(FamilyRole.OWNER, owner.role)
        assertEquals(BigDecimal("5000.00"), owner.monthlyAllowance)
    }
}
