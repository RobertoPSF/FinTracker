package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType
import com.rfilh.fintracker.ui.theme.ActionNavy
import com.rfilh.fintracker.ui.theme.AppIcons
import com.rfilh.fintracker.ui.theme.ExpenseColor
import com.rfilh.fintracker.ui.theme.IncomeColor
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.ReviewViewModel
import kotlin.math.abs

@Composable
fun ReviewScreen(vm: ReviewViewModel, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val colors = MaterialTheme.appColors
    val queue by vm.queue.collectAsStateWithLifecycle()

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconPillButton(AppIcons.ChevronLeft, "Voltar", onBack)
                    Text("Fila de revisão", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (queue.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 70.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Fila vazia", style = MaterialTheme.typography.bodyLarge.copy(color = colors.muted))
                        Text("Nenhuma transação pendente de revisão.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            items(queue, key = { it.id }) { t ->
                ReviewCard(
                    transaction = t,
                    onMarkEntrada = { vm.setType(t, TransactionType.INCOME) },
                    onMarkSaida = { vm.setType(t, TransactionType.EXPENSE) },
                    onApprove = { vm.approve(t) },
                    onEdit = { onEdit(t.id) },
                    onDiscard = { vm.discard(t.id) }
                )
            }
        }
    }
}

@Composable
private fun ReviewCard(
    transaction: Transaction,
    onMarkEntrada: () -> Unit,
    onMarkSaida: () -> Unit,
    onApprove: () -> Unit,
    onEdit: () -> Unit,
    onDiscard: () -> Unit
) {
    val colors = MaterialTheme.appColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(colors.card, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${transaction.bankName} · ${Dates.dayLabel(transaction.occurredAt)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(Money.format(abs(transaction.amountCents)), style = MaterialTheme.typography.titleSmall)
        }
        Text(transaction.counterparty ?: transaction.bankName, style = MaterialTheme.typography.bodyLarge)

        val isEntrada = transaction.type == TransactionType.INCOME
        val isSaida = transaction.type == TransactionType.EXPENSE
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypePickButton("Entrada", isEntrada, IncomeColor, onMarkEntrada, Modifier.weight(1f))
            TypePickButton("Saída", isSaida, ExpenseColor, onMarkSaida, Modifier.weight(1f))
        }

        HorizontalDivider(color = colors.border)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onApprove,
                colors = ButtonDefaults.buttonColors(containerColor = ActionNavy, contentColor = Color.White),
                modifier = Modifier.weight(1f)
            ) { Text("Aprovar") }
            OutlinedButton(
                onClick = onEdit,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ActionNavy),
                border = BorderStroke(1.dp, SolidColor(ActionNavy.copy(alpha = 0.3f))),
                modifier = Modifier.weight(1f)
            ) { Text("Editar") }
            OutlinedButton(
                onClick = onDiscard,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseColor),
                border = BorderStroke(1.dp, SolidColor(ExpenseColor.copy(alpha = 0.3f))),
                modifier = Modifier.weight(1f)
            ) { Text("Descartar") }
        }
    }
}

@Composable
private fun TypePickButton(label: String, active: Boolean, activeColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.appColors
    val bg = if (active) activeColor.copy(alpha = 0.12f) else colors.card
    val border = if (active) activeColor else colors.borderStrong
    Row(
        modifier = modifier
            .background(bg, RoundedCornerShape(10.dp))
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(color = if (active) activeColor else colors.muted))
    }
}
