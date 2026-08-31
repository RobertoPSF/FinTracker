package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.domain.model.MonthBalance
import com.rfilh.fintracker.ui.theme.ExpenseColor
import com.rfilh.fintracker.ui.theme.IncomeColor
import com.rfilh.fintracker.ui.theme.appColors

/** Barra com o topo arredondado e a base reta (apoiada na linha de base do gráfico). */
private fun DrawScope.drawBar(color: Color, topLeft: Offset, size: Size) {
    val radius = CornerRadius(minOf(size.width / 2f, size.height / 2f, 6.dp.toPx()).coerceAtLeast(0f))
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(topLeft, size),
                topLeft = radius,
                topRight = radius,
                bottomLeft = CornerRadius.Zero,
                bottomRight = CornerRadius.Zero
            )
        )
    }
    drawPath(path, color = color)
}

/** RF23: barras entrada/saída lado a lado por mês, sem lib de gráfico (Canvas puro). */
@Composable
fun MonthlyBarChart(months: List<MonthBalance>, barWidth: Dp = 9.dp, modifier: Modifier = Modifier) {
    if (months.isEmpty()) return
    val colors = MaterialTheme.appColors
    val maxValue = months.maxOf { maxOf(it.incomeCents, it.expenseCents) }.coerceAtLeast(1)

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            val slot = size.width / months.size
            val gap = 3.dp.toPx()
            val bw = barWidth.toPx()
            months.forEachIndexed { index, month ->
                val baseX = index * slot + slot / 2f
                val incomeHeight = size.height * (month.incomeCents.toFloat() / maxValue)
                val expenseHeight = size.height * (month.expenseCents.toFloat() / maxValue)
                drawBar(
                    color = IncomeColor,
                    topLeft = Offset(baseX - bw - gap / 2f, size.height - incomeHeight),
                    size = Size(bw, incomeHeight)
                )
                drawBar(
                    color = ExpenseColor,
                    topLeft = Offset(baseX + gap / 2f, size.height - expenseHeight),
                    size = Size(bw, expenseHeight)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            months.forEach { month ->
                Text(Dates.label(month.yearMonth).substringBefore("/"), style = MaterialTheme.typography.labelSmall.copy(color = colors.muted))
            }
        }
    }
}

/** Visão anual: Jan-Dez, meses futuros do ano corrente aparecem acinzentados (sem dados ainda). */
@Composable
fun AnnualBarChart(months: List<MonthBalance>, currentYearMonth: String, modifier: Modifier = Modifier) {
    if (months.isEmpty()) return
    val colors = MaterialTheme.appColors
    val maxValue = months.maxOf { maxOf(it.incomeCents, it.expenseCents) }.coerceAtLeast(1)
    val mutedColor = colors.muted.copy(alpha = colors.muted.alpha * 0.4f)

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            val slot = size.width / months.size
            val gap = 2.dp.toPx()
            val bw = 6.dp.toPx()
            months.forEachIndexed { index, month ->
                val hasData = month.yearMonth <= currentYearMonth
                val incomeColor = if (hasData) IncomeColor else mutedColor
                val expenseColor = if (hasData) ExpenseColor else mutedColor
                val baseX = index * slot + slot / 2f
                val incomeHeight = size.height * (month.incomeCents.toFloat() / maxValue)
                val expenseHeight = size.height * (month.expenseCents.toFloat() / maxValue)
                drawBar(
                    color = incomeColor,
                    topLeft = Offset(baseX - bw - gap / 2f, size.height - incomeHeight),
                    size = Size(bw, incomeHeight)
                )
                drawBar(
                    color = expenseColor,
                    topLeft = Offset(baseX + gap / 2f, size.height - expenseHeight),
                    size = Size(bw, expenseHeight)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            months.forEach { month ->
                val hasData = month.yearMonth <= currentYearMonth
                Text(
                    Dates.label(month.yearMonth).substringBefore("/"),
                    style = MaterialTheme.typography.labelSmall.copy(color = if (hasData) colors.muted else mutedColor)
                )
            }
        }
    }
}
