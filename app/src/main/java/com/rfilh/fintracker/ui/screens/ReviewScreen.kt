package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.domain.model.TransactionType
import com.rfilh.fintracker.ui.vm.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(vm: ReviewViewModel, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val queue by vm.queue.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisão manual") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (queue.isEmpty()) Text("Fila vazia.", modifier = Modifier.padding(16.dp))
            LazyColumn {
                items(queue, key = { it.id }) { t ->
                    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${t.bankName} · ${Dates.dayLabel(t.occurredAt)}")
                            Text(
                                "${t.type} · ${Money.format(t.amountCents)} · ${t.counterparty ?: "sem contraparte"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row {
                                TextButton(onClick = { vm.setType(t, TransactionType.INCOME) }) { Text("Entrada") }
                                TextButton(onClick = { vm.setType(t, TransactionType.EXPENSE) }) { Text("Saída") }
                                TextButton(onClick = { onEdit(t.id) }) { Text("Editar") }
                            }
                            Row {
                                TextButton(onClick = { vm.approve(t) }) { Text("Aprovar") }
                                TextButton(onClick = { vm.discard(t.id) }) { Text("Descartar") }
                            }
                        }
                    }
                }
            }
        }
    }
}
