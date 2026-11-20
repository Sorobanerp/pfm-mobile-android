package br.com.pfm.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OrganicPulseLightColorScheme = lightColorScheme(
    primary = OrganicPulseColors.Primary,
    onPrimary = Color.White,
    primaryContainer = OrganicPulseColors.LimeGlow,
    onPrimaryContainer = OrganicPulseColors.DeepText,
    secondary = OrganicPulseColors.WarmPeach,
    onSecondary = OrganicPulseColors.WarmPeachText,
    error = OrganicPulseColors.CoralCritical,
    onError = Color.White,
    background = OrganicPulseColors.Canvas,
    onBackground = OrganicPulseColors.DeepText,
    surface = OrganicPulseColors.Surface,
    onSurface = OrganicPulseColors.DeepText,
    outline = OrganicPulseColors.Border,
    surfaceVariant = OrganicPulseColors.Canvas,
    onSurfaceVariant = OrganicPulseColors.MutedText
)

@Composable
fun OrganicFinancialPulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OrganicPulseLightColorScheme,
        content = content
    )
}
