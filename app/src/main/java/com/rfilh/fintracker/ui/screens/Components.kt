package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType

val IncomeColor = Color(0xFF1B7F3B)
val ExpenseColor = Color(0xFFB3261E)

@Composable
fun MonthBar(yearMonth: String, onShift: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { onShift(-1) }) { Text("<") }
        Text(Dates.label(yearMonth), style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = { onShift(1) }) { Text(">") }
    }
}

@Composable
fun TransactionRow(transaction: Transaction, onClick: () -> Unit) {
    val sign = if (transaction.type == TransactionType.EXPENSE) "-" else "+"
    val color = when (transaction.type) {
        TransactionType.INCOME -> IncomeColor
        TransactionType.EXPENSE -> ExpenseColor
        TransactionType.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.counterparty ?: transaction.bankName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${transaction.bankName} · ${Dates.dayLabel(transaction.occurredAt)}" +
                        if (transaction.confidence != Confidence.HIGH) " · revisar" else "",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                "$sign ${Money.format(transaction.amountCents)}",
                color = color,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
