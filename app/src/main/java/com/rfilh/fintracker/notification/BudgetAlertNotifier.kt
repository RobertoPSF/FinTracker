package com.rfilh.fintracker.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.data.db.CategoryBudgetDao
import com.rfilh.fintracker.data.db.TransactionDao

/**
 * RF25: dispara notificação local quando o gasto do mês numa categoria estoura o limite
 * configurado. Fica no pacote `notification` (com Context/NotificationManager) para manter
 * o NotificationIngestor livre de Android.
 */
class BudgetAlertNotifier(
    private val categoryBudgetDao: CategoryBudgetDao,
    private val transactionDao: TransactionDao
) {
    suspend fun checkAfterExpense(context: Context, category: String?, yearMonth: String) {
        category ?: return
        val budget = categoryBudgetDao.byCategory(category) ?: return
        if (budget.alertedYearMonth == yearMonth) return

        val spent = transactionDao.categorySpend(category, yearMonth)
        if (spent < budget.limitCents) return

        categoryBudgetDao.markAlerted(category, yearMonth)
        notify(context, category, spent, budget.limitCents)
    }

    private fun notify(context: Context, category: String, spentCents: Long, limitCents: Long) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Limite de $category estourado")
            .setContentText("Gasto: ${Money.format(spentCents)} · Limite: ${Money.format(limitCents)}")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(category.hashCode(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "budget_alerts"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Alertas de limite", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }
}
