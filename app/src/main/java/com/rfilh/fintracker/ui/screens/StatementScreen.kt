package com.rfilh.fintracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.domain.model.TransactionType
import com.rfilh.fintracker.ui.theme.ActionNavy
import com.rfilh.fintracker.ui.theme.ExpenseColor
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.StatementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementScreen(
    vm: StatementViewModel,
    categoryOptions: List<String>,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val colors = MaterialTheme.appColors
    val month by vm.month.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val title by vm.title.collectAsStateWithLifecycle()
    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val filters by vm.filters.collectAsStateWithLifecycle()

    var showFilters by remember { mutableStateOf(false) }

    Scaffold(containerColor = colors.bg) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            item {
                ScreenHeader(title, onBack)

                OutlinedTextField(
                    value = query,
                    onValueChange = { vm.setQuery(it) },
                    placeholder = { Text("Buscar por pessoa ou instituição") },
                    trailingIcon = {
                        if (query.isNotBlank()) TextButton(onClick = { vm.setQuery("") }) { Text("×") }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                if (query.isBlank()) {
                    MonthBar(month) { vm.shiftMonth(it) }
                } else {
                    Text(
                        "Resultado em todos os meses — útil para conferência/auditoria.",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.muted)
                    )
                }

                Spacer(Modifier.height(10.dp))
                FilterToggle(
                    active = filters.activeCount,
                    onClick = { showFilters = !showFilters }
                )
                if (showFilters) {
                    FilterPanel(
                        filters = filters,
                        categoryOptions = categoryOptions,
                        onCategory = vm::setFilterCategory,
                        onType = vm::setFilterType,
                        onMin = vm::setFilterMin,
                        onMax = vm::setFilterMax,
                        onToggleUnreviewed = vm::toggleOnlyUnreviewed,
                        onClear = vm::clearFilters
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (query.isBlank()) "Nenhuma transação encontrada" else "Nenhum resultado para \"$query\"",
                            style = MaterialTheme.typography.bodyLarge.copy(color = colors.muted)
                        )
                        Text(
                            "Ajuste os filtros ou aguarde novas notificações.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(transactions, key = { it.id }) { t ->
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    TransactionRow(t) { onEdit(t.id) }
                }
            }
        }
    }
}

@Composable
private fun FilterToggle(active: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, RoundedCornerShape(12.dp))
            .border(1.dp, colors.borderStrong, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Filtros", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
        if (active > 0) {
            Box(
                modifier = Modifier.background(ActionNavy, RoundedCornerShape(9.dp)).padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(active.toString(), style = MaterialTheme.typography.labelSmall.copy(color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterPanel(
    filters: StatementViewModel.Filters,
    categoryOptions: List<String>,
    onCategory: (String?) -> Unit,
    onType: (TransactionType?) -> Unit,
    onMin: (String) -> Unit,
    onMax: (String) -> Unit,
    onToggleUnreviewed: () -> Unit,
    onClear: () -> Unit
) {
    val colors = MaterialTheme.appColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(colors.card, RoundedCornerShape(14.dp))
            .border(1.dp, colors.borderStrong, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        var catExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
            OutlinedTextField(
                value = filters.category ?: "Todas as categorias",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                DropdownMenuItem(text = { Text("Todas as categorias") }, onClick = { onCategory(null); catExpanded = false })
                categoryOptions.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat) }, onClick = { onCategory(cat); catExpanded = false })
                }
            }
        }

        var typeExpanded by remember { mutableStateOf(false) }
        val typeLabel = when (filters.type) {
            TransactionType.INCOME -> "Entrada"
            TransactionType.EXPENSE -> "Saída"
            TransactionType.UNKNOWN -> "Desconhecido"
            null -> "Todos os tipos"
        }
        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
            OutlinedTextField(
                value = typeLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                DropdownMenuItem(text = { Text("Todos os tipos") }, onClick = { onType(null); typeExpanded = false })
                DropdownMenuItem(text = { Text("Entrada") }, onClick = { onType(TransactionType.INCOME); typeExpanded = false })
                DropdownMenuItem(text = { Text("Saída") }, onClick = { onType(TransactionType.EXPENSE); typeExpanded = false })
                DropdownMenuItem(text = { Text("Desconhecido") }, onClick = { onType(TransactionType.UNKNOWN); typeExpanded = false })
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = filters.minText,
                onValueChange = onMin,
                placeholder = { Text("Valor mín.") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = filters.maxText,
                onValueChange = onMax,
                placeholder = { Text("Valor máx.") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleUnreviewed).padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Só não revisados", style = MaterialTheme.typography.bodyMedium)
            PillToggle(checked = filters.onlyUnreviewed, onCheckedChange = { onToggleUnreviewed() })
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ExpenseColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .clickable(onClick = onClear)
                .padding(vertical = 9.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Limpar filtros", style = MaterialTheme.typography.labelLarge.copy(color = ExpenseColor))
        }
    }
}
