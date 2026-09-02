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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.domain.model.SpendingAlert
import com.rfilh.fintracker.domain.model.AlertKind
import com.rfilh.fintracker.ui.theme.ActionNavy
import com.rfilh.fintracker.ui.theme.CategoryColors
import com.rfilh.fintracker.ui.theme.ExpenseColor
import com.rfilh.fintracker.ui.theme.IncomeColor
import com.rfilh.fintracker.ui.theme.LoanAvatarBg
import com.rfilh.fintracker.ui.theme.ReviewChipBg
import com.rfilh.fintracker.ui.theme.ReviewChipText
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.AlertsViewModel
import com.rfilh.fintracker.ui.vm.CategoriesViewModel
import com.rfilh.fintracker.ui.vm.ComparativoViewModel
import com.rfilh.fintracker.ui.vm.PredictedBillsViewModel
import com.rfilh.fintracker.ui.vm.RankingViewModel
import kotlin.math.roundToInt

/* -------------------------- Gastos por categoria -------------------------- */

@Composable
fun CategoriesScreen(vm: CategoriesViewModel, onBack: () -> Unit, onOpenCategory: (String) -> Unit) {
    val colors = MaterialTheme.appColors
    val month by vm.month.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val total by vm.totalCents.collectAsStateWithLifecycle()

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                ScreenHeader("Gastos por categoria", onBack)
                MonthBar(month) { vm.shiftMonth(it) }
                Text(
                    "${Dates.label(month)} · Total ${Money.format(total)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.muted),
                    modifier = Modifier.padding(top = 12.dp, bottom = 14.dp)
                )
            }

            if (categories.isEmpty()) {
                item { EmptyState("Nenhum gasto categorizado", "Categorize seus lançamentos para ver o comparativo aqui.") }
            }

            items(categories, key = { it.category }) { c ->
                val pct = if (total > 0) (c.cents * 100f / total) else 0f
                val delta = c.deltaPercent
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .background(colors.card, RoundedCornerShape(14.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .clickable { onOpenCategory(c.category) }
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(10.dp).background(CategoryColors.colorFor(c.category), CircleShape))
                            Text(c.category, style = MaterialTheme.typography.bodyLarge)
                        }
                        Text(Money.format(c.cents), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    ProgressTrack(pct / 100f, CategoryColors.colorFor(c.category))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${pct.roundToInt()}% do total", style = MaterialTheme.typography.labelMedium.copy(color = colors.muted))
                        Text(
                            when {
                                delta == null -> "sem base anterior"
                                delta >= 0 -> "+$delta% vs mês anterior"
                                else -> "$delta% vs mês anterior"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = when {
                                    delta == null -> colors.muted
                                    delta > 0 -> ExpenseColor
                                    else -> IncomeColor
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

/* ------------------------ Ranking de contrapartes ------------------------ */

@Composable
fun RankingScreen(vm: RankingViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.appColors
    val expense by vm.expense.collectAsStateWithLifecycle()
    val ranking by vm.ranking.collectAsStateWithLifecycle()

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                ScreenHeader("Ranking de contrapartes", onBack)
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SegmentButton("Mais gastei com", expense, { vm.setExpense(true) }, Modifier.weight(1f))
                    SegmentButton("Mais recebi de", !expense, { vm.setExpense(false) }, Modifier.weight(1f))
                }
            }

            if (ranking.isEmpty()) {
                item { EmptyState("Sem histórico ainda", "Assim que houver lançamentos suficientes, o ranking aparece aqui.") }
            }

            items(ranking, key = { it.name }) { r ->
                val i = ranking.indexOf(r) + 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(colors.card, RoundedCornerShape(14.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#$i", style = MaterialTheme.typography.labelMedium.copy(color = colors.muted, fontWeight = FontWeight.Bold))
                    Box(
                        modifier = Modifier.size(38.dp).background(LoanAvatarBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text(r.name.take(1).uppercase(), style = MaterialTheme.typography.bodyLarge.copy(color = ActionNavy)) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(r.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                        Text(
                            if (r.count == 1) "1 lançamento" else "${r.count} lançamentos",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(Money.format(r.cents), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

/* --------------------------- Comparativo mensal --------------------------- */

@Composable
fun ComparativoScreen(vm: ComparativoViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.appColors
    val month by vm.month.collectAsStateWithLifecycle()
    val comparison by vm.comparison.collectAsStateWithLifecycle()

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                ScreenHeader("Comparativo mensal", onBack)
                MonthBar(month) { vm.shiftMonth(it) }
            }

            val c = comparison
            if (c == null) {
                item { EmptyState("Sem dados", "Ainda não há lançamentos suficientes para comparar.") }
            } else {
              item {
                SectionHeader(
                    "${Dates.label(c.current.yearMonth)} vs ${Dates.label(c.previous.yearMonth)}",
                    Modifier.padding(top = 16.dp)
                )
                CompareCard(
                    "Entradas", IncomeColor,
                    c.current.incomeCents, c.previous.incomeCents,
                    Dates.label(c.current.yearMonth), Dates.label(c.previous.yearMonth)
                )
                CompareCard(
                    "Saídas", ExpenseColor,
                    c.current.expenseCents, c.previous.expenseCents,
                    Dates.label(c.current.yearMonth), Dates.label(c.previous.yearMonth),
                    invertDeltaColor = true
                )
                SectionHeader("Vs. mesmo mês do ano anterior", Modifier.padding(top = 8.dp))
                val ly = c.sameMonthLastYear
                if (ly == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.card, RoundedCornerShape(14.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ainda não temos dados de ${Dates.label(c.current.yearMonth)} do ano passado para comparar.",
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.muted)
                        )
                    }
                } else {
                    CompareCard(
                        "Entradas", IncomeColor,
                        c.current.incomeCents, ly.incomeCents,
                        Dates.label(c.current.yearMonth), Dates.label(ly.yearMonth)
                    )
                    CompareCard(
                        "Saídas", ExpenseColor,
                        c.current.expenseCents, ly.expenseCents,
                        Dates.label(c.current.yearMonth), Dates.label(ly.yearMonth),
                        invertDeltaColor = true
                    )
                }
              }
            }
        }
    }
}

@Composable
private fun CompareCard(
    label: String,
    barColor: Color,
    currentCents: Long,
    baselineCents: Long,
    currentLabel: String,
    baselineLabel: String,
    invertDeltaColor: Boolean = false
) {
    val colors = MaterialTheme.appColors
    val max = maxOf(currentCents, baselineCents, 1L)
    val delta: Int? = if (baselineCents <= 0) null else ((currentCents - baselineCents) * 100 / baselineCents).toInt()
    val deltaColor = when {
        delta == null || delta == 0 -> colors.muted
        (delta > 0) != invertDeltaColor -> IncomeColor
        else -> ExpenseColor
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(colors.card, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                if (delta == null) "—" else "${if (delta >= 0) "+" else ""}$delta%",
                style = MaterialTheme.typography.labelMedium.copy(color = deltaColor)
            )
        }
        CompareRow(currentLabel, currentCents, max, barColor, colors.text)
        CompareRow(baselineLabel, baselineCents, max, barColor.copy(alpha = 0.4f), colors.muted)
    }
}

@Composable
private fun CompareRow(label: String, cents: Long, max: Long, barColor: Color, valueColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.appColors.muted), modifier = Modifier.width(64.dp))
        Box(modifier = Modifier.weight(1f)) { ProgressTrack(cents.toFloat() / max, barColor) }
        Text(Money.format(cents), style = MaterialTheme.typography.labelMedium.copy(color = valueColor))
    }
}

/* --------------------------- Contas previstas --------------------------- */

@Composable
fun PredictedBillsScreen(vm: PredictedBillsViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.appColors
    val bills by vm.bills.collectAsStateWithLifecycle()

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                ScreenHeader("Contas previstas", onBack)
                Text(
                    "Baseado nos gastos recorrentes detectados.",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.muted),
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            if (bills.isEmpty()) {
                item { EmptyState("Nada previsto", "Nenhuma recorrência detectada para projetar cobranças.") }
            }

            items(bills, key = { it.name }) { b ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(colors.card, RoundedCornerShape(14.dp))
                        .border(1.dp, if (b.soon) ExpenseColor.copy(alpha = 0.3f) else colors.border, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .size(44.dp)
                            .background(if (b.soon) ExpenseColor.copy(alpha = 0.12f) else colors.border, RoundedCornerShape(12.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "%02d".format(Dates.dayOfMonth(b.dueAt)),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (b.soon) ExpenseColor else colors.text
                            )
                        )
                        Text(
                            Dates.monthShort(b.dueAt).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(color = if (b.soon) ExpenseColor else colors.muted)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(b.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                        Text(
                            when {
                                b.daysUntil <= 0 -> "Vence hoje"
                                b.daysUntil == 1 -> "Vence em 1 dia"
                                else -> "Vence em ${b.daysUntil} dias"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (b.soon) ExpenseColor else colors.muted,
                                fontWeight = if (b.soon) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                    Text(Money.format(b.amountCents), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

/* -------------------------------- Alertas -------------------------------- */

@Composable
fun AlertsScreen(vm: AlertsViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.appColors
    val alerts by vm.alerts.collectAsStateWithLifecycle()

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item { ScreenHeader("Alertas", onBack) }

            if (alerts.isEmpty()) {
                item { EmptyState("Nenhum alerta", "Sem reajustes, gastos atípicos ou resumos relevantes agora.") }
            }

            items(alerts) { a ->
                AlertCard(a)
            }
        }
    }
}

@Composable
private fun AlertCard(a: SpendingAlert) {
    val colors = MaterialTheme.appColors
    val (tag, tagColor, tagBg) = when (a.kind) {
        AlertKind.REAJUSTE -> Triple("REAJUSTE", ReviewChipText, ReviewChipBg)
        AlertKind.ATIPICO -> Triple("ATÍPICO", Color(0xFFB33A3A), Color(0xFFFBE9E9))
        AlertKind.RESUMO -> Triple("RESUMO", ActionNavy, Color(0xFFEAF0F5))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(colors.card, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(tagBg, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text(tag, style = MaterialTheme.typography.labelSmall.copy(color = tagColor, fontWeight = FontWeight.Bold))
            }
            Text(Dates.shortDayMonth(a.atMillis), style = MaterialTheme.typography.labelSmall.copy(color = colors.muted))
        }
        Text(a.text, style = MaterialTheme.typography.bodyMedium)
    }
}

/* ------------------------------- shared ui ------------------------------- */

@Composable
internal fun SegmentButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.appColors
    Row(
        modifier = modifier
            .background(if (selected) ActionNavy else colors.card, RoundedCornerShape(10.dp))
            .border(1.dp, if (selected) ActionNavy else colors.borderStrong, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(color = if (selected) Color.White else colors.text))
    }
}

