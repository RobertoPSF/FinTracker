package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.ui.theme.ActionNavy
import com.rfilh.fintracker.ui.theme.AppIcons
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.ManualPixViewModel
import java.util.Calendar
import java.util.TimeZone

/**
 * Registro manual de Pix enviado — para bancos/apps que ainda não notificam saída de Pix
 * de forma automática (ex. Mercado Pago, Nubank em certos casos). O usuário informa valor,
 * banco de origem e destinatário; a transação entra direto como saída confirmada, sem
 * passar pela fila de revisão.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualPixScreen(vm: ManualPixViewModel, onDone: () -> Unit) {
    val colors = MaterialTheme.appColors
    val t by vm.state.collectAsStateWithLifecycle()

    var amountText by remember { mutableStateOf("") }
    var counterparty by remember { mutableStateOf("") }
    var otherBankText by remember { mutableStateOf("") }
    var useOtherBank by remember { mutableStateOf(false) }
    var bankExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(containerColor = colors.bg) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconPillButton(AppIcons.ChevronLeft, "Voltar", onDone)
                Text("Registrar Pix enviado", style = MaterialTheme.typography.titleMedium)
            }

            Text(
                "Para bancos ou apps que ainda não notificam envio de Pix automaticamente. " +
                    "O valor entra como saída assim que você registrar.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.muted),
                modifier = Modifier.padding(bottom = 18.dp)
            )

            FieldLabel("Valor")
            OutlinedTextField(
                value = amountText,
                onValueChange = { value ->
                    amountText = value
                    Money.parseBrlToCents(value)?.let { cents -> vm.update { it.copy(amountCents = cents) } }
                },
                placeholder = { Text("0,00") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            )

            FieldLabel("Banco de origem")
            ExposedDropdownMenuBox(
                expanded = bankExpanded,
                onExpandedChange = { bankExpanded = it },
                modifier = Modifier.padding(bottom = if (useOtherBank) 10.dp else 14.dp)
            ) {
                OutlinedTextField(
                    value = if (useOtherBank) "Outro banco" else t.bankName.ifBlank { "Selecione" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = bankExpanded, onDismissRequest = { bankExpanded = false }) {
                    vm.bankOptions.forEach { (pkg, name) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = {
                            useOtherBank = false
                            vm.update { it.copy(bankPackage = pkg, bankName = name) }
                            bankExpanded = false
                        })
                    }
                    DropdownMenuItem(text = { Text("Outro banco") }, onClick = {
                        useOtherBank = true
                        vm.update { it.copy(bankPackage = "", bankName = "") }
                        bankExpanded = false
                    })
                }
            }

            if (useOtherBank) {
                OutlinedTextField(
                    value = otherBankText,
                    onValueChange = { value ->
                        otherBankText = value
                        vm.update { it.copy(bankPackage = ManualPixViewModel.manualPackageFor(value), bankName = value) }
                    },
                    placeholder = { Text("Nome do banco") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                )
            }

            FieldLabel("Data")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .background(colors.card, RoundedCornerShape(12.dp))
                    .border(1.dp, colors.borderStrong, RoundedCornerShape(12.dp))
                    .clickable { showDatePicker = true }
                    .padding(14.dp)
            ) {
                Text(Dates.dateLabel(t.occurredAt), style = MaterialTheme.typography.bodyMedium)
            }

            FieldLabel("Para quem foi")
            OutlinedTextField(
                value = counterparty,
                onValueChange = { counterparty = it; vm.update { tx -> tx.copy(counterparty = it.ifBlank { null }) } },
                placeholder = { Text("Nome do destinatário") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            )

            Button(
                onClick = { vm.save(onDone) },
                colors = ButtonDefaults.buttonColors(containerColor = ActionNavy, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) { Text("Registrar") }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = t.occurredAt)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { vm.setOccurredAt(toLocalNoon(it)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = pickerState) }
    }
}

/**
 * O DatePicker do Material3 devolve meia-noite UTC do dia escolhido — convertido direto pro
 * fuso local isso pode cair no dia anterior (ex. Brasil, UTC-3). Recalcula pro meio-dia local
 * do mesmo dia de calendário pra evitar esse desvio.
 */
private fun toLocalNoon(utcMidnightMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMidnightMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 12, 0)
    }.timeInMillis
}
