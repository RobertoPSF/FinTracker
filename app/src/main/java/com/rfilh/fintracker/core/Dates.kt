package com.rfilh.fintracker.core

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object Dates {
    /** epochMillis -> "yyyy-MM" (fuso local) */
    fun yearMonth(epochMillis: Long): String {
        val c = Calendar.getInstance()
        c.timeInMillis = epochMillis
        return String.format(Locale.US, "%04d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
    }

    fun currentYearMonth(): String = yearMonth(System.currentTimeMillis())

    /** "2026-08", delta -1 -> "2026-07" */
    fun shift(yearMonth: String, delta: Int): String {
        val year = yearMonth.substring(0, 4).toInt()
        val month = yearMonth.substring(5, 7).toInt()
        val total = year * 12 + (month - 1) + delta
        return String.format(Locale.US, "%04d-%02d", total / 12, total % 12 + 1)
    }

    fun label(yearMonth: String): String {
        val names = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")
        val m = yearMonth.substring(5, 7).toInt()
        return "${names[m - 1]}/${yearMonth.substring(0, 4)}"
    }

    /** epochMillis -> "31/08/2026", sem hora — usado onde não há horário real (ex. lançamento manual). */
    fun dateLabel(epochMillis: Long): String {
        val c = Calendar.getInstance(TimeZone.getDefault())
        c.timeInMillis = epochMillis
        return String.format(
            Locale.US, "%02d/%02d/%04d",
            c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR)
        )
    }

    fun dayLabel(epochMillis: Long): String {
        val c = Calendar.getInstance(TimeZone.getDefault())
        c.timeInMillis = epochMillis
        return String.format(
            Locale.US, "%02d/%02d %02d:%02d",
            c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1,
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)
        )
    }
}
