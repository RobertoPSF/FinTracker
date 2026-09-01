package com.rfilh.fintracker.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Ícones recriados a partir dos `path d="..."` do protótipo Claude Design (fidelidade exata,
 * em vez de Material Icons genéricos). Desenhados em preto sólido de propósito — a cor real
 * vem do `tint` passado a `Icon(...)` no local de uso (equivalente ao `var(--primary)` do mock).
 */
object AppIcons {

    private fun ImageVector.Builder.strokePath(
        pathData: String,
        strokeWidth: Float,
        cap: StrokeCap = StrokeCap.Round,
        join: StrokeJoin = StrokeJoin.Round
    ) = addPath(
        pathData = addPathNodes(pathData),
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = strokeWidth,
        strokeLineCap = cap,
        strokeLineJoin = join,
        pathFillType = PathFillType.NonZero
    )

    private fun singleStroke(name: String, width: Float, height: Float, strokeWidth: Float, pathData: String): ImageVector =
        ImageVector.Builder(name = name, defaultWidth = width.dp, defaultHeight = height.dp, viewportWidth = width, viewportHeight = height)
            .apply { strokePath(pathData, strokeWidth) }
            .build()

    val ChevronLeft: ImageVector by lazy { singleStroke("ChevronLeft", 9f, 15f, 2f, "M 7.5 1.5 L 1.5 7.5 L 7.5 13.5") }

    val ChevronRight: ImageVector by lazy { singleStroke("ChevronRight", 9f, 15f, 2f, "M 1.5 1.5 L 7.5 7.5 L 1.5 13.5") }

    val Settings: ImageVector by lazy {
        // Glifo padrão do Material Design (ic_settings) em vez da engrenagem desenhada à mão
        // no protótipo: essa era um esboço irregular no próprio design de origem — trocado
        // por um ícone simétrico e testado, mantendo só a cor/tint do app.
        ImageVector.Builder(name = "Settings", defaultWidth = 18.dp, defaultHeight = 18.dp, viewportWidth = 24f, viewportHeight = 24f)
            .apply {
                // Espaçado a partir do glifo AOSP ic_settings original (mecanicamente, via
                // script — evita erro de transcrição manual) para não repetir o problema de
                // notação compacta encontrado antes; conferido pixel a pixel contra o original.
                addPath(
                    pathData = addPathNodes(
                        "M 19.14 12.94 c 0.04 -0.3 0.06 -0.61 0.06 -0.94 c 0 -0.32 -0.02 -0.64 -0.07 -0.94 " +
                            "l 2.03 -1.58 c 0.18 -0.14 0.23 -0.41 0.12 -0.61 l -1.92 -3.32 c -0.12 -0.22 -0.37 -0.29 -0.59 -0.22 " +
                            "l -2.39 0.96 c -0.5 -0.38 -1.03 -0.7 -1.62 -0.94 L 14.4 2.81 c -0.04 -0.24 -0.24 -0.41 -0.48 -0.41 " +
                            "h -3.84 c -0.24 0 -0.43 0.17 -0.47 0.41 L 9.25 5.35 C 8.66 5.59 8.12 5.92 7.63 6.29 L 5.24 5.33 " +
                            "c -0.22 -0.08 -0.47 0 -0.59 0.22 L 2.74 8.87 C 2.62 9.08 2.66 9.34 2.86 9.48 l 2.03 1.58 " +
                            "C 4.84 11.36 4.8 11.69 4.8 12 s 0.02 0.64 0.07 0.94 l -2.03 1.58 c -0.18 0.14 -0.23 0.41 -0.12 0.61 " +
                            "l 1.92 3.32 c 0.12 0.22 0.37 0.29 0.59 0.22 l 2.39 -0.96 c 0.5 0.38 1.03 0.7 1.62 0.94 " +
                            "l 0.36 2.54 c 0.05 0.24 0.24 0.41 0.48 0.41 h 3.84 c 0.24 0 0.44 -0.17 0.47 -0.41 l 0.36 -2.54 " +
                            "c 0.59 -0.24 1.13 -0.56 1.62 -0.94 l 2.39 0.96 c 0.22 0.08 0.47 0 0.59 -0.22 l 1.92 -3.32 " +
                            "c 0.12 -0.22 0.07 -0.47 -0.12 -0.61 L 19.14 12.94 z M 12 15.6 c -1.98 0 -3.6 -1.62 -3.6 -3.6 " +
                            "s 1.62 -3.6 3.6 -3.6 s 3.6 1.62 3.6 3.6 S 13.98 15.6 12 15.6 z"
                    ),
                    fill = SolidColor(Color.Black)
                )
            }.build()
    }

    val Extrato: ImageVector by lazy {
        ImageVector.Builder(name = "Extrato", defaultWidth = 20.dp, defaultHeight = 20.dp, viewportWidth = 24f, viewportHeight = 24f)
            .apply {
                strokePath(
                    "M 7 3 L 17 3 A 2 2 0 0 1 19 5 L 19 19 A 2 2 0 0 1 17 21 L 7 21 A 2 2 0 0 1 5 19 L 5 5 A 2 2 0 0 1 7 3 Z",
                    strokeWidth = 1.6f
                )
                strokePath("M 8 8 L 16 8 M 8 12 L 16 12 M 8 16 L 13 16", strokeWidth = 1.6f)
            }.build()
    }

    val Cobrar: ImageVector by lazy {
        ImageVector.Builder(name = "Cobrar", defaultWidth = 20.dp, defaultHeight = 20.dp, viewportWidth = 24f, viewportHeight = 24f)
            .apply {
                strokePath("M 4 10 A 5 5 0 1 0 14 10 A 5 5 0 1 0 4 10", strokeWidth = 1.6f)
                strokePath("M 11 15 A 5 5 0 1 0 21 15 A 5 5 0 1 0 11 15", strokeWidth = 1.6f)
            }.build()
    }

    val Grafico: ImageVector by lazy { singleStroke("Grafico", 24f, 24f, 2f, "M 5 20 L 5 13 M 12 20 L 12 6 M 19 20 L 19 10") }

    val Plus: ImageVector by lazy { singleStroke("Plus", 24f, 24f, 2f, "M 12 5 L 12 19 M 5 12 L 19 12") }

    val Bell: ImageVector by lazy {
        ImageVector.Builder(name = "Bell", defaultWidth = 20.dp, defaultHeight = 20.dp, viewportWidth = 24f, viewportHeight = 24f)
            .apply {
                strokePath(
                    "M 18 8 A 6 6 0 0 0 6 8 C 6 15 3 17 3 17 L 21 17 C 21 17 18 15 18 8 Z",
                    strokeWidth = 1.6f
                )
                strokePath("M 13.73 21 A 2 2 0 0 1 10.27 21", strokeWidth = 1.6f)
            }.build()
    }

    val Trash: ImageVector by lazy {
        singleStroke("Trash", 24f, 24f, 1.6f, "M 5 7 L 19 7 M 9 7 L 9 4 L 15 4 L 15 7 M 6 7 L 7 20 L 17 20 L 18 7")
    }
}
