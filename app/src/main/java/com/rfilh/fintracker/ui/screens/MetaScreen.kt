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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.ui.theme.ActionNavy
import com.rfilh.fintracker.ui.theme.IncomeColor
import com.rfilh.fintracker.ui.theme.appColors
import com.rfilh.fintracker.ui.vm.MetaViewModel
import kotlin.math.roundToInt

@Composable
fun MetaScreen(vm: MetaViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.appColors
    val month by vm.month.collectAsStateWithLifecycle()
    val goalCents by vm.goalCents.collectAsStateWithLifecycle()
    val balance by vm.balance.collectAsStateWithLifecycle()

    var goalText by remember(goalCents) {
        mutableStateOf(if (goalCents > 0) (goalCents / 100.0).toString().replace('.', ',') else "")
    }

    val saved = balance.netCents.coerceAtLeast(0)
    val pct = if (goalCents > 0) (saved * 100f / goalCents).coerceIn(0f, 100f) else 0f
    val onTrack = goalCents > 0 && saved >= goalCents

    Scaffold(containerColor = colors.bg) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())
        ) {
            ScreenHeader("Meta de economia", onBack)
            MonthBar(month) { vm.shiftMonth(it) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(colors.card, RoundedCornerShape(18.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Meta para ${Dates.label(month)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.muted)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it },
                        placeholder = { Text("1500,00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(ActionNavy, RoundedCornerShape(12.dp))
                            .clickable { vm.setGoal(goalText) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text("Salvar", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Economizado até agora", style = MaterialTheme.typography.bodySmall.copy(color = colors.muted))
                    Text(Money.format(saved), style = MaterialTheme.typography.bodyMedium)
                }
                ProgressTrack(pct / 100f, if (onTrack) IncomeColor else ActionNavy, height = 10.dp)
                Text(
                    when {
                        goalCents <= 0 -> "Defina uma meta para acompanhar o progresso do mês."
                        onTrack -> "Meta batida! Você economizou ${Money.format(saved)} este mês."
                        else -> "Faltam ${Money.format(goalCents - saved)} para atingir a meta (${pct.roundToInt()}%)."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.muted)
                )
            }
        }
    }
}
