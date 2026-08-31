package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.notification.NotificationAccess
import com.rfilh.fintracker.ui.vm.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOpenBank: (String) -> Unit,
    onOpenAll: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenReceivables: () -> Unit
) {
    val context = LocalContext.current
    val month by vm.month.collectAsStateWithLifecycle()
    val balance by vm.balance.collectAsStateWithLifecycle()
    val banks by vm.banks.collectAsStateWithLifecycle()
    val reviewCount by vm.reviewCount.collectAsStateWithLifecycle()
    val captured by vm.capturedCount.collectAsStateWithLifecycle()

    var accessGranted by remember { mutableStateOf(NotificationAccess.isEnabled(context)) }

    Scaffold(topBar = { TopAppBar(title = { Text("FinTracker") }) }) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            if (!accessGranted) {
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Acesso a notificações desativado", fontWeight = FontWeight.Bold)
                        Text(
                            "Sem essa permissão o app não captura transações.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row {
                            Button(onClick = { context.startActivity(NotificationAccess.settingsIntent()) }) {
                                Text("Conceder")
                            }
                            TextButton(onClick = { accessGranted = NotificationAccess.isEnabled(context) }) {
                                Text("Já concedi")
                            }
                        }
                    }
                }
            }

            MonthBar(month) { vm.shiftMonth(it) }

            Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Saldo do mês", style = MaterialTheme.typography.labelMedium)
                    Text(
                        Money.format(balance.netCents),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Entradas ${Money.format(balance.incomeCents)}", color = IncomeColor)
                        Text("Saídas ${Money.format(balance.expenseCents)}", color = ExpenseColor)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onOpenAll) { Text("Extrato geral") }
                TextButton(onClick = onOpenReview) { Text("Revisar ($reviewCount)") }
                TextButton(onClick = onOpenReceivables) { Text("A cobrar") }
            }

            Text(
                "Notificações capturadas: $captured",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn {
                items(banks, key = { it.bankPackage }) { bank ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onOpenBank(bank.bankPackage) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(bank.bankName, fontWeight = FontWeight.Medium)
                            Text("${bank.count} transações", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(Money.format(bank.incomeCents - bank.expenseCents))
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
