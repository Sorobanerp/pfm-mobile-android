package br.com.pfm.mobile.domain

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * RBAC roles for family governance (US-13).
 */
enum class FamilyRole {
    OWNER,
    MEMBER,
    VIEWER
}

data class FamilyMemberSummary(
    val id: String,
    val displayName: String,
    val email: String,
    val role: FamilyRole,
    val monthlyAllowance: BigDecimal
)

/**
 * Personal inflation alert per EAN item scanned via NFC-e QR Code (US-11).
 */
data class PersonalInflationAlert(
    val ean: String,
    val itemDescription: String,
    val previousPrice: BigDecimal,
    val currentPrice: BigDecimal,
    val deltaPercentage: BigDecimal,
    val isAlertTriggered: Boolean
)

object PersonalInflationEvaluator {
    private val HUNDRED = BigDecimal("100")

    fun evaluate(
        ean: String,
        itemDescription: String,
        previousPrice: BigDecimal,
        currentPrice: BigDecimal,
        thresholdPercentage: BigDecimal = BigDecimal("5.00")
    ): PersonalInflationAlert {
        require(previousPrice > BigDecimal.ZERO) { "previousPrice must be positive" }
        require(currentPrice > BigDecimal.ZERO) { "currentPrice must be positive" }

        val deltaPct = currentPrice
            .subtract(previousPrice)
            .divide(previousPrice, 4, RoundingMode.HALF_EVEN)
            .multiply(HUNDRED)
            .setScale(2, RoundingMode.HALF_EVEN)

        return PersonalInflationAlert(
            ean = ean.trim(),
            itemDescription = itemDescription.trim(),
            previousPrice = previousPrice.setScale(2, RoundingMode.HALF_EVEN),
            currentPrice = currentPrice.setScale(2, RoundingMode.HALF_EVEN),
            deltaPercentage = deltaPct,
            isAlertTriggered = deltaPct >= thresholdPercentage
        )
    }
}
