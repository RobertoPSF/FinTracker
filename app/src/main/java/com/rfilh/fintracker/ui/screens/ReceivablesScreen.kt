package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.ui.theme.AppIcons
import com.rfilh.fintracker.ui.theme.ExpenseOnPrimary
import com.rfilh.fintracker.ui.theme.LoanAvatarBg
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.ReceivablesViewModel

@Composable
fun ReceivablesScreen(vm: ReceivablesViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.appColors
    val people by vm.people.collectAsStateWithLifecycle()

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconPillButton(AppIcons.ChevronLeft, "Voltar", onBack)
                    Text("A cobrar", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (people.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 70.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nada pendente", style = MaterialTheme.typography.bodyLarge.copy(color = colors.muted))
                        Text("Você não tem empréstimos a cobrar de ninguém.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            items(people, key = { it.person }) { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(colors.card, RoundedCornerShape(14.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).background(LoanAvatarBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(p.person.take(1).uppercase(), style = MaterialTheme.typography.bodyLarge.copy(color = colors.primary))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.person, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (p.count == 1) "1 lançamento" else "${p.count} lançamentos",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(Money.format(p.pendingCents), style = MaterialTheme.typography.bodyLarge.copy(color = ExpenseOnPrimary))
                }
            }
        }
    }
}
