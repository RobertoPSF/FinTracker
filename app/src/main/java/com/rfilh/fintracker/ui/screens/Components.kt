package com.rfilh.fintracker.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.data.BankIconCache
import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType
import com.rfilh.fintracker.ui.theme.AppIcons
import com.rfilh.fintracker.ui.theme.BankPalette
import com.rfilh.fintracker.ui.theme.ExpenseColor
import com.rfilh.fintracker.ui.theme.IncomeColor
import com.rfilh.fintracker.ui.theme.ReviewBadgeBg
import com.rfilh.fintracker.ui.theme.ReviewChipBg
import com.rfilh.fintracker.ui.theme.ReviewChipText
import com.rfilh.fintracker.ui.theme.appColors

@Composable
fun MonthBar(yearMonth: String, onShift: (Int) -> Unit) {
    val colors = MaterialTheme.appColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconGhostButton(AppIcons.ChevronLeft, "Mês anterior", onClick = { onShift(-1) })
        Text(Dates.label(yearMonth), style = MaterialTheme.typography.titleSmall)
        IconGhostButton(AppIcons.ChevronRight, "Próximo mês", onClick = { onShift(1) })
    }
}

/** Botão circular sem fundo, usado nas setas de navegação de mês. */
@Composable
private fun IconGhostButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(30.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = MaterialTheme.appColors.primary, modifier = Modifier.size(9.dp))
    }
}

/** Botão circular com fundo e borda — voltar, configurações, ações do topo das telas. */
@Composable
fun IconPillButton(icon: ImageVector, description: String, onClick: () -> Unit, size: Dp = 36.dp, badgeCount: Int = 0) {
    val colors = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .size(size)
            .background(colors.card, RoundedCornerShape(11.dp))
            .border(1.dp, colors.borderStrong, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = MaterialTheme.appColors.primary, modifier = Modifier.size(size * 0.42f))
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .background(ReviewBadgeBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badgeCount.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Ícone real do app do banco quando já capturado (ver [BankIconCache]); cai pra quadrado
 * colorido com a(s) inicial(is) do banco/pessoa enquanto não houver ícone em cache.
 */
@Composable
fun AvatarBadge(
    color: Color,
    initial: String,
    packageName: String? = null,
    size: Dp = 38.dp,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp)
) {
    val context = LocalContext.current
    val icon = remember(packageName) {
        packageName
            ?.let { BankIconCache.iconFile(context, it) }
            ?.takeIf { it.exists() }
            ?.let { runCatching { BitmapFactory.decodeFile(it.path)?.asImageBitmap() }.getOrNull() }
    }
    Box(
        modifier = Modifier.size(size).clip(shape).background(if (icon != null) Color.Transparent else color),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Image(icon, contentDescription = null, modifier = Modifier.fillMaxSize())
        } else {
            Text(initial, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge.copy(color = Color.White))
        }
    }
}

/** Switch customizado (pill com knob deslizante), fiel ao protótipo. */
@Composable
fun PillToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.appColors
    val trackColor = if (checked) colors.primary else colors.trackOff
    val knobOffset by animateDpAsState(if (checked) 21.dp else 3.dp, label = "knob")
    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 28.dp)
            .background(trackColor, RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .padding(top = 3.dp, start = knobOffset)
                .size(22.dp)
                .background(colors.card, CircleShape)
        )
    }
}

@Composable
fun TransactionRow(transaction: Transaction, onClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    // Tipo desconhecido fica neutro: não é entrada nem saída até o usuário revisar.
    val (amountBg, sign) = when (transaction.type) {
        TransactionType.INCOME -> IncomeColor to "+ "
        TransactionType.EXPENSE -> ExpenseColor to "− "
        TransactionType.UNKNOWN -> colors.muted to ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarBadge(BankPalette.colorFor(transaction.bankPackage), BankPalette.initialFor(transaction.bankName), packageName = transaction.bankPackage)
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    transaction.bankName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                if (transaction.confidence != Confidence.HIGH) {
                    Box(
                        modifier = Modifier.background(ReviewChipBg, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("REVISAR", color = ReviewChipText, style = MaterialTheme.typography.labelSmall.copy(color = ReviewChipText))
                    }
                }
                if (transaction.manual) {
                    Box(
                        modifier = Modifier.background(colors.border, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("MANUAL", color = colors.muted, style = MaterialTheme.typography.labelSmall.copy(color = colors.muted))
                    }
                }
            }
            val meta = listOfNotNull(
                Dates.dayLabel(transaction.occurredAt),
                transaction.counterparty,
                transaction.category
            ).joinToString(" · ")
            Text(meta, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
        Box(
            modifier = Modifier
                .background(amountBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                sign + Money.format(transaction.amountCents),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Bold),
                maxLines = 1
            )
        }
    }
}

/** Cabeçalho padrão das telas internas: botão voltar + título (+ ação opcional à direita). */
@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconPillButton(AppIcons.ChevronLeft, "Voltar", onBack)
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

/** Rótulo de seção em caixa alta (CONTAS CONECTADAS, GASTOS RECORRENTES, ...). */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.appColors.muted),
        modifier = modifier.padding(bottom = 10.dp)
    )
}

/** Estado vazio centralizado usado em várias telas de lista. */
@Composable
fun EmptyState(title: String, subtitle: String) {
    val colors = MaterialTheme.appColors
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge.copy(color = colors.muted))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
    }
}

/** Barra de progresso fina (limites de categoria, meta de economia). */
@Composable
fun ProgressTrack(fraction: Float, color: Color, height: Dp = 8.dp) {
    val colors = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(colors.border)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}

/** Rótulo pequeno acima de um campo de formulário (Editar, Registrar Pix enviado). */
@Composable
fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.appColors.muted), modifier = Modifier.padding(bottom = 6.dp))
}

/** Botão do grid 2x2 de atalhos da Home (ícone + rótulo, com badge opcional). */
@Composable
fun ShortcutCard(icon: ImageVector, label: String, badgeCount: Int = 0, onClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .aspectRatio(1.55f)
            .background(colors.card, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, label, tint = colors.primary, modifier = Modifier.size(20.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(ReviewBadgeBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(badgeCount.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
            }
        }
    }
}
