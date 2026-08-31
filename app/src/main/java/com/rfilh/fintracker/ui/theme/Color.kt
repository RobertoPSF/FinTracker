package com.rfilh.fintracker.ui.theme

import androidx.compose.ui.graphics.Color

/** Tokens extraídos do protótipo Claude Design ("Orça.dc.html"). */
data class AppColors(
    val bg: Color,
    val card: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val borderStrong: Color,
    val primary: Color,
    val trackOff: Color
)

val LightAppColors = AppColors(
    bg = Color(0xFFF4F6F8),
    card = Color(0xFFFFFFFF),
    text = Color(0xFF14212E),
    muted = Color(0x8014212E),
    border = Color(0x0F14212E),
    borderStrong = Color(0x1414212E),
    primary = Color(0xFF1B3A5C),
    trackOff = Color(0x2614212E)
)

val DarkAppColors = AppColors(
    bg = Color(0xFF12161B),
    card = Color(0xFF1E242C),
    text = Color(0xFFF2F4F6),
    muted = Color(0x8CF2F4F6),
    border = Color(0x14FFFFFF),
    borderStrong = Color(0x1FFFFFFF),
    primary = Color(0xFF8FB8DE),
    trackOff = Color(0x2EFFFFFF)
)

// Cores fixas: não mudam entre claro/escuro.
/** Botões de ação principal (Salvar/Aprovar/Adicionar) e o card de saldo da Home — sempre navy, mesmo no escuro. */
val ActionNavy = Color(0xFF1B3A5C)
val IncomeColor = Color(0xFF1E8E5A)
val ExpenseColor = Color(0xFFD64545)
val IncomeOnPrimary = Color(0xFF8CE0B4)
val ExpenseOnPrimary = Color(0xFFFF9D6C)

val WarningBg = Color(0xFFFFF4E8)
val WarningBorder = Color(0xFFFFD3AE)
val WarningText = Color(0xFF8A4A1F)

val ReviewChipBg = Color(0xFFFFEBD2)
val ReviewChipText = Color(0xFFB36A17)
val ReviewBadgeBg = Color(0xFFD64545)

val DangerBg = Color(0xFFFFF0F0)
val LoanAvatarBg = Color(0xFFEAF0F5)

val ToastBg = Color(0xFF14212E)
val ToastText = Color(0xFFFFFFFF)
