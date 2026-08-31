package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.domain.model.LoanStatus
import com.rfilh.fintracker.domain.model.TransactionType
import com.rfilh.fintracker.ui.vm.EditViewModel
import com.rfilh.fintracker.ui.vm.LoanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(vm: EditViewModel, onDone: () -> Unit) {
    val transaction by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar transação") },
                navigationIcon = { TextButton(onClick = onDone) { Text("Voltar") } }
            )
        }
    ) { padding ->
        val t = transaction
        if (t == null) {
            Text("Carregando...", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }

        var amountText by remember(t.id) { mutableStateOf((t.amountCents / 100.0).toString()) }
        var counterparty by remember(t.id) { mutableStateOf(t.counterparty.orEmpty()) }
        var category by remember(t.id) { mutableStateOf(t.category.orEmpty()) }
        var note by remember(t.id) { mutableStateOf(t.note.orEmpty()) }
        var loanPerson by remember(t.id) { mutableStateOf(t.loanPerson.orEmpty()) }

        Column(
            modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            Text("${t.bankName} · ${Money.format(t.amountCents)}", style = MaterialTheme.typography.titleMedium)

            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                TransactionType.entries.forEach { type ->
                    FilterChip(
                        selected = t.type == type,
                        onClick = { vm.update { it.copy(type = type) } },
                        label = { Text(type.name) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { value ->
                    amountText = value
                    Money.parseBrlToCents(value)?.let { cents -> vm.update { it.copy(amountCents = cents) } }
                },
                label = { Text("Valor") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = counterparty,
                onValueChange = { counterparty = it; vm.update { tx -> tx.copy(counterparty = it.ifBlank { null }) } },
                label = { Text("Contraparte") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it; vm.update { tx -> tx.copy(category = it.ifBlank { null }) } },
                label = { Text("Categoria") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it; vm.update { tx -> tx.copy(note = it.ifBlank { null }) } },
                label = { Text("Observação") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Empréstimo", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp))
            Column {
                LoanOptions.all.forEach { (status, label) ->
                    FilterChip(
                        selected = t.loanStatus == status,
                        onClick = { vm.update { it.copy(loanStatus = status) } },
                        label = { Text(label) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            if (t.loanStatus != LoanStatus.NONE) {
                OutlinedTextField(
                    value = loanPerson,
                    onValueChange = { loanPerson = it; vm.update { tx -> tx.copy(loanPerson = it.ifBlank { null }) } },
                    label = { Text("Pessoa") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(modifier = Modifier.padding(top = 16.dp)) {
                Button(onClick = { vm.save(onDone) }) { Text("Salvar") }
                TextButton(onClick = { vm.delete(onDone) }) { Text("Excluir") }
            }
        }
    }
}
