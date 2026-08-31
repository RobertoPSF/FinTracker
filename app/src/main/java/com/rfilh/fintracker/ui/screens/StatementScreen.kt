package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.ui.vm.StatementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementScreen(vm: StatementViewModel, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val month by vm.month.collectAsStateWithLifecycle()
    val transactions by vm.transactions.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Extrato") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            MonthBar(month) { vm.shiftMonth(it) }
            if (transactions.isEmpty()) {
                Text("Sem transações neste mês.", modifier = Modifier.padding(16.dp))
            }
            LazyColumn {
                items(transactions, key = { it.id }) { t ->
                    TransactionRow(t) { onEdit(t.id) }
                }
            }
        }
    }
}
