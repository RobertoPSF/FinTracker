package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.notification.NotificationAccess
import com.rfilh.fintracker.ui.theme.ActionNavy
import com.rfilh.fintracker.ui.theme.AppIcons
import com.rfilh.fintracker.ui.theme.BankPalette
import com.rfilh.fintracker.ui.theme.ExpenseColor
import com.rfilh.fintracker.ui.theme.ExpenseOnPrimary
import com.rfilh.fintracker.ui.theme.IncomeColor
import com.rfilh.fintracker.ui.theme.IncomeOnPrimary
import com.rfilh.fintracker.ui.theme.WarningBg
import com.rfilh.fintracker.ui.theme.WarningBorder
import com.rfilh.fintracker.ui.theme.WarningText
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.HomeViewModel
import kotlin.math.abs

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOpenBank: (String) -> Unit,
    onOpenAll: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenReceivables: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val context = LocalContext.current
    val month by vm.month.collectAsStateWithLifecycle()
    val balance by vm.balance.collectAsStateWithLifecycle()
    val banks by vm.banks.collectAsStateWithLifecycle()
    val reviewCount by vm.reviewCount.collectAsStateWithLifecycle()

    var accessGranted by remember { mutableStateOf(NotificationAccess.isEnabled(context)) }

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("FinTracker", style = MaterialTheme.typography.titleLarge)
                    IconPillButton(AppIcons.Settings, "Configurações", onOpenSettings, size = 38.dp)
                }
            }

            if (!accessGranted) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarningBg, RoundedCornerShape(14.dp))
                            .border(1.dp, WarningBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Acesso a notificações desativado", style = MaterialTheme.typography.bodyLarge.copy(color = WarningText))
                        Text(
                            "Ative a permissão para continuar capturando lançamentos automaticamente.",
                            style = MaterialTheme.typography.bodySmall.copy(color = WarningText)
                        )
                        Row {
                            TextButton(
                                onClick = { context.startActivity(NotificationAccess.settingsIntent()) },
                                colors = ButtonDefaults.textButtonColors(containerColor = ActionNavy, contentColor = Color.White)
                            ) { Text("Conceder permissão") }
                            TextButton(onClick = { accessGranted = NotificationAccess.isEnabled(context) }) { Text("Já concedi") }
                        }
                    }
                    Spacer(Modifier.size(16.dp))
                }
            }

            item {
                MonthBar(month) { vm.shiftMonth(it) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .background(ActionNavy, RoundedCornerShape(20.dp))
                        .padding(22.dp)
                ) {
                    Text("Saldo do mês", style = MaterialTheme.typography.bodySmall.copy(color = Color(0x99FFFFFF)))
                    Text(
                        (if (balance.netCents >= 0) "+" else "-") + Money.format(abs(balance.netCents)),
                        style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                        modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f).background(Color(0x14FFFFFF), RoundedCornerShape(12.dp)).padding(12.dp)) {
                            Text("Entradas", style = MaterialTheme.typography.labelMedium.copy(color = Color(0x99FFFFFF)))
                            Text("+" + Money.format(balance.incomeCents), style = MaterialTheme.typography.bodyLarge.copy(color = IncomeOnPrimary))
                        }
                        Column(modifier = Modifier.weight(1f).background(Color(0x14FFFFFF), RoundedCornerShape(12.dp)).padding(12.dp)) {
                            Text("Saídas", style = MaterialTheme.typography.labelMedium.copy(color = Color(0x99FFFFFF)))
                            Text("-" + Money.format(balance.expenseCents), style = MaterialTheme.typography.bodyLarge.copy(color = ExpenseOnPrimary))
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.weight(1f)) { ShortcutCard(AppIcons.Extrato, "Extrato geral", onClick = onOpenAll) }
                        Row(modifier = Modifier.weight(1f)) { ShortcutCard(AppIcons.Fila, "Fila de revisão", badgeCount = reviewCount, onClick = onOpenReview) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.weight(1f)) { ShortcutCard(AppIcons.Cobrar, "A cobrar", onClick = onOpenReceivables) }
                        Row(modifier = Modifier.weight(1f)) { ShortcutCard(AppIcons.Grafico, "Gráfico", onClick = onOpenInsights) }
                    }
                }
                Text(
                    "CONTAS CONECTADAS",
                    style = MaterialTheme.typography.labelLarge.copy(color = colors.muted),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            items(banks, key = { it.bankPackage }) { bank ->
                val net = bank.incomeCents - bank.expenseCents
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(colors.card, RoundedCornerShape(14.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .clickable { onOpenBank(bank.bankPackage) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AvatarBadge(BankPalette.colorFor(bank.bankPackage), BankPalette.initialFor(bank.bankName))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(bank.bankName, style = MaterialTheme.typography.bodyLarge)
                        Text(if (bank.count == 1) "1 transação" else "${bank.count} transações", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        (if (net >= 0) "+" else "-") + Money.format(abs(net)),
                        style = MaterialTheme.typography.bodyLarge.copy(color = if (net >= 0) IncomeColor else ExpenseColor)
                    )
                }
            }

            item { Spacer(Modifier.size(24.dp)) }
        }
    }
}
