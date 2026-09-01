package com.rfilh.fintracker.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.domain.model.LoanStatus
import com.rfilh.fintracker.domain.model.TransactionType
import com.rfilh.fintracker.ui.theme.ActionNavy
import com.rfilh.fintracker.ui.theme.AppIcons
import com.rfilh.fintracker.ui.theme.BankPalette
import com.rfilh.fintracker.ui.theme.ExpenseColor
import com.rfilh.fintracker.ui.theme.IncomeColor
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.EditViewModel
import com.rfilh.fintracker.ui.vm.LoanOptions
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(vm: EditViewModel, onDone: () -> Unit) {
    val colors = MaterialTheme.appColors
    val transaction by vm.state.collectAsStateWithLifecycle()

    Scaffold(containerColor = colors.bg) { padding ->
        val t = transaction
        if (t == null) {
            Text("Carregando...", modifier = Modifier.padding(padding).padding(20.dp))
            return@Scaffold
        }

        var amountText by remember(t.id) { mutableStateOf((t.amountCents / 100.0).toString()) }
        var counterparty by remember(t.id) { mutableStateOf(t.counterparty.orEmpty()) }
        var note by remember(t.id) { mutableStateOf(t.note.orEmpty()) }
        var loanPerson by remember(t.id) { mutableStateOf(t.loanPerson.orEmpty()) }
        val isLoan = t.loanStatus != LoanStatus.NONE

        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconPillButton(AppIcons.ChevronLeft, "Voltar", onDone)
                    Text("Editar lançamento", style = MaterialTheme.typography.titleMedium)
                }
                IconPillButton(AppIcons.Trash, "Excluir", { vm.delete(onDone) })
            }

            val signed = if (t.type == TransactionType.EXPENSE) -abs(t.amountCents) else abs(t.amountCents)
            val amountColor = if (signed >= 0) IncomeColor else ExpenseColor
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
                    .background(amountColor.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AvatarBadge(BankPalette.colorFor(t.bankPackage), BankPalette.initialFor(t.bankName), packageName = t.bankPackage, size = 26.dp, shape = RoundedCornerShape(8.dp))
                    Text(t.bankName, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    (if (signed >= 0) "+" else "-") + Money.format(abs(signed)),
                    style = MaterialTheme.typography.headlineMedium.copy(color = amountColor)
                )
            }

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeOption("Entrada", t.type == TransactionType.INCOME, { vm.update { it.copy(type = TransactionType.INCOME) } }, Modifier.weight(1f))
                TypeOption("Saída", t.type == TransactionType.EXPENSE, { vm.update { it.copy(type = TransactionType.EXPENSE) } }, Modifier.weight(1f))
                TypeOption("Desconhecido", t.type == TransactionType.UNKNOWN, { vm.update { it.copy(type = TransactionType.UNKNOWN) } }, Modifier.weight(1f))
            }

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

            FieldLabel("Contraparte")
            OutlinedTextField(
                value = counterparty,
                onValueChange = { counterparty = it; vm.update { tx -> tx.copy(counterparty = it.ifBlank { null }) } },
                placeholder = { Text("Nome") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            )

            FieldLabel("Categoria")
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                OutlinedTextField(
                    value = t.category ?: "Sem categoria",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    DropdownMenuItem(text = { Text("Sem categoria") }, onClick = {
                        vm.update { it.copy(category = null) }; categoryExpanded = false
                    })
                    vm.categoryOptions.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = {
                            vm.update { it.copy(category = cat) }; categoryExpanded = false
                        })
                    }
                }
            }

            FieldLabel("Observação")
            OutlinedTextField(
                value = note,
                onValueChange = { note = it; vm.update { tx -> tx.copy(note = it.ifBlank { null }) } },
                placeholder = { Text("Adicionar nota") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .background(colors.card, RoundedCornerShape(14.dp))
                    .border(1.dp, colors.borderStrong, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Marcar como empréstimo", style = MaterialTheme.typography.bodyLarge)
                PillToggle(checked = isLoan, onCheckedChange = { checked ->
                    vm.update { it.copy(loanStatus = if (checked) LoanStatus.LENT_PENDING else LoanStatus.NONE) }
                })
            }

            if (isLoan) {
                FieldLabel("Status do empréstimo")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 14.dp)) {
                    LoanOptions.all.forEach { (status, label) ->
                        val selected = t.loanStatus == status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (selected) colors.primary.copy(alpha = 0.08f) else colors.card, RoundedCornerShape(10.dp))
                                .border(1.dp, if (selected) colors.primary else colors.border, RoundedCornerShape(10.dp))
                                .clickable { vm.update { it.copy(loanStatus = status) } }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .border(2.dp, if (selected) colors.primary else colors.muted, CircleShape)
                                    .then(if (selected) Modifier.background(colors.primary, CircleShape) else Modifier)
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                FieldLabel("Pessoa")
                OutlinedTextField(
                    value = loanPerson,
                    onValueChange = { loanPerson = it; vm.update { tx -> tx.copy(loanPerson = it.ifBlank { null }) } },
                    placeholder = { Text("Nome da pessoa") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                )
            }

            Button(
                onClick = { vm.save(onDone) },
                colors = ButtonDefaults.buttonColors(containerColor = ActionNavy, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
            ) { Text("Salvar") }
            TextButton(
                onClick = { vm.delete(onDone) },
                colors = ButtonDefaults.textButtonColors(contentColor = ExpenseColor),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) { Text("Excluir lançamento") }
        }
    }
}

@Composable
private fun TypeOption(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.appColors
    Row(
        modifier = modifier
            .background(if (selected) ActionNavy else colors.card, RoundedCornerShape(10.dp))
            .border(1.dp, if (selected) ActionNavy else colors.borderStrong, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(color = if (selected) Color.White else colors.text))
    }
}
