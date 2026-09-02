package com.rfilh.fintracker.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.ui.theme.ActionNavy
import com.rfilh.fintracker.ui.theme.ExpenseOnPrimary
import com.rfilh.fintracker.ui.theme.LoanAvatarBg
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.ReceivablesViewModel

@Composable
fun ReceivablesScreen(vm: ReceivablesViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.appColors
    val context = LocalContext.current
    val people by vm.people.collectAsStateWithLifecycle()

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item { ScreenHeader("A cobrar", onBack) }

            if (people.isEmpty()) {
                item { EmptyState("Nada pendente", "Você não tem empréstimos a cobrar de ninguém.") }
            }

            items(people, key = { it.person }) { p ->
                var showForm by remember(p.person) { mutableStateOf(false) }
                var payText by remember(p.person) { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(colors.card, RoundedCornerShape(14.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).background(LoanAvatarBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(p.person.take(1).uppercase(), style = MaterialTheme.typography.bodyLarge.copy(color = ActionNavy))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.person, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (p.count == 1) "1 lançamento" else "${p.count} lançamentos",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                Money.format(p.remainingCents),
                                style = MaterialTheme.typography.bodyLarge.copy(color = ExpenseOnPrimary, fontWeight = FontWeight.Bold)
                            )
                            if (p.partiallyPaid) {
                                Text(
                                    "de ${Money.format(p.pendingCents)}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = colors.muted)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SegmentButton(
                            "Pagamento parcial",
                            selected = showForm,
                            onClick = { showForm = !showForm },
                            modifier = Modifier.weight(1f)
                        )
                        SegmentButton(
                            "Cobrar no WhatsApp",
                            selected = true,
                            onClick = {
                                val msg = "${p.person}, você me deve ${Money.format(p.remainingCents)} " +
                                    "(${p.count} ${if (p.count == 1) "lançamento" else "lançamentos"})."
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, msg)
                                }
                                runCatching { context.startActivity(Intent.createChooser(send, "Cobrar")) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (showForm) {
                        HorizontalDivider(color = colors.border)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = payText,
                                onValueChange = { payText = it },
                                placeholder = { Text("Valor recebido") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .background(ActionNavy, RoundedCornerShape(10.dp))
                                    .clickable {
                                        vm.registerPayment(p.person, payText)
                                        payText = ""
                                        showForm = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text("OK", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                            }
                        }
                    }
                }
            }
        }
    }
}
