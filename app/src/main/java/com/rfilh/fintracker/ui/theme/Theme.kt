package com.rfilh.fintracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LocalAppColors = compositionLocalOf { LightAppColors }

/** Acesso direto aos tokens do protótipo (equivalente às variáveis CSS --bg/--card/etc). */
val MaterialTheme.appColors: AppColors
    @Composable get() = LocalAppColors.current

/**
 * Sem `color` nos estilos: um TextStyle com cor explícita sempre vence sobre o
 * `LocalContentColor` ambiente, o que quebraria o texto branco de botões preenchidos
 * (Salvar/Aprovar/Adicionar) e qualquer outro `Text` sem `color` próprio dentro de um
 * container colorido. `bodySmall` é a exceção deliberada: é o estilo "secundário/muted"
 * usado em dezenas de legendas fora de botões, então a cor muted fica fixa nele.
 */
private fun typography(colors: AppColors) = Typography().let { base ->
    Typography(
        headlineMedium = base.headlineMedium.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 32.sp),
        titleLarge = base.titleLarge.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 22.sp),
        titleMedium = base.titleMedium.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 19.sp),
        titleSmall = base.titleSmall.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
        bodyLarge = base.bodyLarge.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        bodySmall = base.bodySmall.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, color = colors.muted),
        labelLarge = base.labelLarge.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
        labelMedium = base.labelMedium.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
        labelSmall = base.labelSmall.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 10.5.sp)
    )
}

@Composable
fun FinTrackerTheme(darkMode: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val appColors = if (darkMode) DarkAppColors else LightAppColors
    val colorScheme = if (darkMode) {
        darkColorScheme(
            background = appColors.bg, surface = appColors.card, primary = appColors.primary,
            onBackground = appColors.text, onSurface = appColors.text
        )
    } else {
        lightColorScheme(
            background = appColors.bg, surface = appColors.card, primary = appColors.primary,
            onBackground = appColors.text, onSurface = appColors.text
        )
    }
    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(colorScheme = colorScheme, typography = typography(appColors), content = content)
    }
}
