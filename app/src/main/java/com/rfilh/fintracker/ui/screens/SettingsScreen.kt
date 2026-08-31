package com.rfilh.fintracker.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.ui.theme.ActionNavy
import com.rfilh.fintracker.ui.theme.AppIcons
import com.rfilh.fintracker.ui.theme.DangerBg
import com.rfilh.fintracker.ui.theme.ExpenseColor
import com.rfilh.fintracker.ui.theme.ToastBus
import com.rfilh.fintracker.ui.theme.WarningBg
import com.rfilh.fintracker.ui.theme.WarningBorder
import com.rfilh.fintracker.ui.theme.WarningText
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.appColors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val appLockEnabled by vm.appLockEnabled.collectAsStateWithLifecycle()
    val autoPurgeRaw by vm.autoPurgeRaw.collectAsStateWithLifecycle()
    val darkMode by vm.darkMode.collectAsStateWithLifecycle()
    val budgets by vm.categoryBudgets.collectAsStateWithLifecycle()

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    val exportBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val json = vm.exportBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            ToastBus.show("Backup exportado")
        }
    }
    val importBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (json != null) vm.importBackup(json)
    }
    val exportUnrecognizedLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val json = vm.exportUnrecognizedJson()
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            ToastBus.show("Notificações exportadas (anonimizado)")
        }
    }

    var showAddLimit by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf("") }
    var newLimit by remember { mutableStateOf("") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val availableCategories = vm.categoryOptions.filter { c -> budgets.none { it.category == c } }

    Scaffold(containerColor = colors.bg) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconPillButton(AppIcons.ChevronLeft, "Voltar", onBack)
                Text("Configurações", style = MaterialTheme.typography.titleMedium)
            }

            SectionLabel("Aparência")
            SettingsRow("Tema escuro") { PillToggle(checked = darkMode, onCheckedChange = { vm.setDarkMode(it) }) }
            Spacer18()

            SectionLabel("Segurança")
            if (!vm.biometricAvailable) {
                InfoBanner("Seu aparelho não tem biometria ou PIN configurado. Configure-os no sistema para ativar o bloqueio.")
            }
            SettingsRow("Bloqueio biométrico / PIN", enabled = vm.biometricAvailable) {
                PillToggle(checked = appLockEnabled && vm.biometricAvailable, onCheckedChange = { vm.setAppLockEnabled(it) })
            }
            Spacer18()

            SectionLabel("Notificações")
            SettingsRow("Descartar conteúdo bruto automaticamente") {
                PillToggle(checked = autoPurgeRaw, onCheckedChange = { vm.setAutoPurgeRaw(it) })
            }
            SettingsButton("Apagar conteúdo bruto agora") { vm.purgeNow() }
            SettingsButton("Exportar notificações não reconhecidas (anonimizado)") {
                exportUnrecognizedLauncher.launch("fintracker-nao-reconhecidas.json")
            }
            Spacer18()

            SectionLabel("Backup")
            SettingsButton("Exportar backup") { exportBackupLauncher.launch("fintracker-backup.json") }
            SettingsButton("Restaurar backup") { importBackupLauncher.launch(arrayOf("application/json")) }
            Spacer18()

            SectionLabel("Limites de gasto por categoria")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                budgets.forEach { budget ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.card, RoundedCornerShape(12.dp))
                            .border(1.dp, colors.borderStrong, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(budget.category, style = MaterialTheme.typography.bodyLarge)
                            Text("${Money.format(budget.limitCents)} / mês", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(
                            modifier = Modifier
                                .size(28.dp)
                                .background(DangerBg, RoundedCornerShape(8.dp))
                                .clickable { vm.deleteCategoryBudget(budget.category) },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) { Text("×", color = ExpenseColor, style = MaterialTheme.typography.titleSmall.copy(color = ExpenseColor)) }
                    }
                }
            }

            if (showAddLimit) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .background(colors.card, RoundedCornerShape(14.dp))
                        .border(1.dp, colors.borderStrong, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExposedDropdownMenuBox(expanded = categoryMenuExpanded, onExpandedChange = { categoryMenuExpanded = it }) {
                        OutlinedTextField(
                            value = newCategory.ifBlank { "Selecionar categoria" },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                            availableCategories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat) }, onClick = { newCategory = cat; categoryMenuExpanded = false })
                            }
                        }
                    }
                    OutlinedTextField(
                        value = newLimit,
                        onValueChange = { newLimit = it },
                        placeholder = { Text("Valor (R$)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val cents = Money.parseBrlToCents(newLimit)
                                if (newCategory.isNotBlank() && cents != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    vm.saveCategoryBudget(newCategory, cents)
                                    newCategory = ""; newLimit = ""; showAddLimit = false
                                } else {
                                    ToastBus.show("Preencha categoria e valor")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ActionNavy, contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) { Text("Adicionar") }
                        TextButton(onClick = { showAddLimit = false }, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .clickable { showAddLimit = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("+ Adicionar limite", style = MaterialTheme.typography.bodyMedium.copy(color = colors.primary))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.appColors.muted),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun Spacer18() {
    Spacer(Modifier.height(18.dp))
}

@Composable
private fun SettingsRow(label: String, enabled: Boolean = true, trailing: @Composable () -> Unit) {
    val colors = MaterialTheme.appColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(colors.card, RoundedCornerShape(14.dp))
            .border(1.dp, colors.borderStrong, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge.copy(color = if (enabled) colors.text else colors.muted), modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun SettingsButton(label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(colors.card, RoundedCornerShape(12.dp))
            .border(1.dp, colors.borderStrong, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun InfoBanner(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(WarningBg, RoundedCornerShape(12.dp))
            .border(1.dp, WarningBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall.copy(color = WarningText))
    }
}
