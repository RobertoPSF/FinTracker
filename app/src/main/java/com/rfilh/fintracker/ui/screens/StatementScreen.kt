package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.ui.theme.AppIcons
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.StatementViewModel

@Composable
fun StatementScreen(vm: StatementViewModel, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val colors = MaterialTheme.appColors
    val month by vm.month.collectAsStateWithLifecycle()
    val title by vm.title.collectAsStateWithLifecycle()
    val transactions by vm.transactions.collectAsStateWithLifecycle()

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconPillButton(AppIcons.ChevronLeft, "Voltar", onBack)
                    Text(title, style = MaterialTheme.typography.titleMedium)
                }
                MonthBar(month) { vm.shiftMonth(it) }
                Spacer(Modifier.height(16.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 70.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nenhuma transação neste mês", style = MaterialTheme.typography.bodyLarge.copy(color = colors.muted))
                        Text(
                            "Assim que novas notificações forem capturadas, elas aparecem aqui.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(transactions, key = { it.id }) { t ->
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    TransactionRow(t) { onEdit(t.id) }
                }
            }
        }
    }
}
