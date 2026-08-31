package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.ui.vm.ReceivablesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivablesScreen(vm: ReceivablesViewModel, onBack: () -> Unit) {
    val people by vm.people.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("A cobrar") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (people.isEmpty()) Text("Nada pendente.", modifier = Modifier.padding(16.dp))
            LazyColumn {
                items(people, key = { it.person }) { p ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(p.person)
                            Text("${p.count} lançamentos", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(Money.format(p.pendingCents))
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
