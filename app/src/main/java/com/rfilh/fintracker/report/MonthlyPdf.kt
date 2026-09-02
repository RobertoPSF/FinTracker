package com.rfilh.fintracker.report

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.core.RecurringItem
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType
import java.io.OutputStream
import kotlin.math.abs

/** Dados brutos que o relatório mensal precisa — montados na ViewModel, renderizados aqui. */
data class MonthlyReportInput(
    val yearMonth: String,
    val monthTransactions: List<Transaction>,
    val recurring: List<RecurringItem>
)

/**
 * Relatório mensal em PDF (uma página A4), gerado com `android.graphics.pdf.PdfDocument` —
 * sem dependência externa. Layout simples: cabeçalho, balanço, gastos por categoria e
 * recorrentes.
 */
object MonthlyPdf {

    private const val PAGE_W = 595 // A4 @ 72dpi
    private const val PAGE_H = 842
    private const val MARGIN = 48f

    fun write(out: OutputStream, input: MonthlyReportInput) {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
        draw(page.canvas, input)
        doc.finishPage(page)
        doc.writeTo(out)
        doc.close()
    }

    private fun draw(canvas: Canvas, input: MonthlyReportInput) {
        val title = Paint().apply { color = Color.parseColor("#14212E"); textSize = 22f; isFakeBoldText = true }
        val h2 = Paint().apply { color = Color.parseColor("#1B3A5C"); textSize = 13f; isFakeBoldText = true }
        val body = Paint().apply { color = Color.parseColor("#14212E"); textSize = 12f }
        val muted = Paint().apply { color = Color.parseColor("#6B7684"); textSize = 11f }
        val income = Paint().apply { color = Color.parseColor("#1E8E5A"); textSize = 12f }
        val expense = Paint().apply { color = Color.parseColor("#D64545"); textSize = 12f }

        var y = MARGIN + 8f
        canvas.drawText("FinTracker — relatório de ${Dates.label(input.yearMonth)}", MARGIN, y, title)
        y += 20f
        canvas.drawText("Gerado em ${Dates.dateLabel(System.currentTimeMillis())}", MARGIN, y, muted)
        y += 32f

        val confirmed = input.monthTransactions.filter { it.reviewed }
        val incomeCents = confirmed.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents }
        val expenseCents = confirmed.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
        val net = incomeCents - expenseCents

        canvas.drawText("BALANÇO DO MÊS", MARGIN, y, h2); y += 18f
        canvas.drawText("Entradas", MARGIN, y, body); canvas.drawText("+ ${Money.format(incomeCents)}", MARGIN + 260f, y, income); y += 16f
        canvas.drawText("Saídas", MARGIN, y, body); canvas.drawText("- ${Money.format(expenseCents)}", MARGIN + 260f, y, expense); y += 16f
        canvas.drawText("Saldo", MARGIN, y, body)
        canvas.drawText("${if (net >= 0) "+" else "-"} ${Money.format(abs(net))}", MARGIN + 260f, y, if (net >= 0) income else expense)
        y += 32f

        canvas.drawText("GASTOS POR CATEGORIA", MARGIN, y, h2); y += 18f
        val byCategory = confirmed
            .filter { it.type == TransactionType.EXPENSE && !it.category.isNullOrBlank() }
            .groupBy { it.category!! }
            .mapValues { (_, list) -> list.sumOf { it.amountCents } }
            .toList().sortedByDescending { it.second }
        if (byCategory.isEmpty()) {
            canvas.drawText("Nenhum gasto categorizado neste mês.", MARGIN, y, muted); y += 18f
        } else {
            byCategory.forEach { (cat, cents) ->
                canvas.drawText(cat, MARGIN, y, body)
                canvas.drawText(Money.format(cents), MARGIN + 260f, y, body)
                y += 16f
            }
        }
        y += 20f

        canvas.drawText("GASTOS RECORRENTES", MARGIN, y, h2); y += 18f
        if (input.recurring.isEmpty()) {
            canvas.drawText("Nenhuma recorrência detectada.", MARGIN, y, muted); y += 18f
        } else {
            input.recurring.forEach { r ->
                canvas.drawText("${r.counterparty} · ${r.monthsSeen.size} meses", MARGIN, y, body)
                canvas.drawText(Money.format(r.avgAmountCents), MARGIN + 260f, y, body)
                y += 16f
            }
        }
    }
}
