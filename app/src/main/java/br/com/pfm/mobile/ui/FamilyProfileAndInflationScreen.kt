package br.com.pfm.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.pfm.mobile.domain.FamilyMemberSummary
import br.com.pfm.mobile.domain.PersonalInflationAlert
import br.com.pfm.mobile.ui.theme.OrganicFinancialPulseTheme
import br.com.pfm.mobile.ui.theme.OrganicPulseColors

/**
 * Offline-First Compose Screen for Personal Inflation Alerts (US-11) & Family Profile (US-13).
 */
@Composable
fun FamilyProfileAndInflationScreen(
    members: List<FamilyMemberSummary>,
    inflationAlerts: List<PersonalInflationAlert>
) {
    OrganicFinancialPulseTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OrganicPulseColors.Canvas)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Perfil Familiar (US-13) & Inflação Pessoal (US-11)",
                style = MaterialTheme.typography.titleLarge,
                color = OrganicPulseColors.DeepText
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(inflationAlerts) { alert ->
                    val container = if (alert.isAlertTriggered) {
                        OrganicPulseColors.WarmPeach
                    } else {
                        OrganicPulseColors.LimeGlow
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = container)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${alert.itemDescription} (EAN ${alert.ean})",
                                style = MaterialTheme.typography.titleSmall,
                                color = OrganicPulseColors.DeepText
                            )
                            Text(
                                text = "R$ ${alert.previousPrice} -> R$ ${alert.currentPrice} (+${alert.deltaPercentage}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OrganicPulseColors.WarmPeachText
                            )
                        }
                    }
                }

                items(members) { member ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = OrganicPulseColors.Surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = member.displayName, color = OrganicPulseColors.DeepText)
                                Text(text = member.email, color = OrganicPulseColors.MutedText)
                            }
                            Text(
                                text = "${member.role} • R$ ${member.monthlyAllowance}",
                                color = OrganicPulseColors.Primary
                            )
                        }
                    }
                }
            }
        }
    }
}
