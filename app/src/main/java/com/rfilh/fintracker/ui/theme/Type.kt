package com.rfilh.fintracker.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.rfilh.fintracker.R

/**
 * Space Grotesk (SIL Open Font License), fonte variável — os 3 pesos usados no protótipo
 * são instanciados via eixo "wght". Em API < 26 o eixo é ignorado e o Android renderiza
 * a instância padrão da fonte para todos os pesos (degradação graciosa, sem crash).
 */
@OptIn(ExperimentalTextApi::class)
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.space_grotesk, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.space_grotesk, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)
