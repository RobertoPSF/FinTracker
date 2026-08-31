package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.ui.theme.AppIcons
import com.rfilh.fintracker.ui.theme.ExpenseColor
import com.rfilh.fintracker.ui.theme.IncomeColor
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.GraficoTab
import com.rfilh.fintracker.ui.vm.InsightsViewModel
import kotlin.math.abs

@Composable
fun InsightsScreen(vm: InsightsViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.appColors
    val tab by vm.tab.collectAsStateWithLifecycle()
    val trend by vm.trend.collectAsStateWithLifecycle()
    val yearlyTrend by vm.yearlyTrend.collectAsStateWithLifecycle()
    val recurring by vm.recurring.collectAsStateWithLifecycle()

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconPillButton(AppIcons.ChevronLeft, "Voltar", onBack)
                    Text("Visão geral", style = MaterialTheme.typography.titleMedium)
                }

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TabButton("Mensal", tab == GraficoTab.MENSAL, { vm.selectTab(GraficoTab.MENSAL) }, Modifier.weight(1f))
                    TabButton("Anual", tab == GraficoTab.ANUAL, { vm.selectTab(GraficoTab.ANUAL) }, Modifier.weight(1f))
                }

                if (tab == GraficoTab.ANUAL) {
                    val income = yearlyTrend.sumOf { it.incomeCents }
                    val expense = yearlyTrend.sumOf { it.expenseCents }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryStat("Entradas", "+" + Money.format(income), IncomeColor, Modifier.weight(1f))
                        SummaryStat("Saídas", "-" + Money.format(expense), ExpenseColor, Modifier.weight(1f))
                        SummaryStat("Saldo", (if (income - expense >= 0) "+" else "-") + Money.format(abs(income - expense)), colors.text, Modifier.weight(1f))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                        .background(colors.card, RoundedCornerShape(16.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(top = 16.dp, start = 12.dp, end = 12.dp, bottom = 10.dp)
                ) {
                    if (tab == GraficoTab.MENSAL) {
                        MonthlyBarChart(trend)
                    } else {
                        AnnualBarChart(yearlyTrend, Dates.currentYearMonth())
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = colors.border)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LegendDot(IncomeColor, "Entradas")
                        Box(Modifier.size(16.dp))
                        LegendDot(ExpenseColor, "Saídas")
                    }
                }

                Text(
                    "GASTOS RECORRENTES",
                    style = MaterialTheme.typography.labelLarge.copy(color = colors.muted),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            if (recurring.isEmpty()) {
                item {
                    Text(
                        "Nenhuma recorrência detectada ainda.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            }

            items(recurring, key = { it.counterparty }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(colors.card, RoundedCornerShape(14.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.counterparty, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Média · identificado em ${item.monthsSeen.size} meses",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(Money.format(item.avgAmountCents), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun TabButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.appColors
    Row(
        modifier = modifier
            .background(if (selected) colors.primary else colors.card, RoundedCornerShape(10.dp))
            .border(1.dp, if (selected) colors.primary else colors.borderStrong, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(color = if (selected) colors.card else colors.text))
    }
}

@Composable
private fun SummaryStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.appColors
    Column(
        modifier = modifier
            .background(colors.card, RoundedCornerShape(12.dp))
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(color = colors.muted))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(color = color))
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.appColors.muted))
    }
}
