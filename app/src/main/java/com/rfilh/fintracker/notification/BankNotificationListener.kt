package com.rfilh.fintracker.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.rfilh.fintracker.FinTrackerApp
import com.rfilh.fintracker.domain.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BankNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val container get() = (applicationContext as FinTrackerApp).container

    override fun onListenerConnected() {
        Log.i(TAG, "listener conectado")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        if (!container.rules().isKnownBank(pkg)) return
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
            Log.d(TAG, "$pkg: ignorado (FLAG_GROUP_SUMMARY)")
            return
        }

        val extras = sbn.notification.extras
        if (extras == null) {
            Log.w(TAG, "$pkg: notificação sem extras")
            return
        }
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                ?.joinToString("\n") { it.toString() }
        if (title.isNullOrBlank() && text.isNullOrBlank()) {
            Log.w(TAG, "$pkg: sem título/texto extraível (extras: ${extras.keySet().joinToString()})")
            return
        }

        val incoming = NotificationIngestor.Incoming(
            packageName = pkg,
            title = title,
            body = text,
            postedAt = if (sbn.postTime > 0) sbn.postTime else System.currentTimeMillis()
        )

        scope.launch {
            runCatching { container.ingestor.ingest(incoming) }
                .onSuccess { outcome ->
                    Log.d(TAG, "$pkg: ${outcome::class.simpleName}")
                    if (outcome is NotificationIngestor.Outcome.Stored && outcome.type == TransactionType.EXPENSE) {
                        runCatching {
                            container.budgetAlertNotifier.checkAfterExpense(
                                applicationContext, outcome.category, outcome.yearMonth
                            )
                        }.onFailure { Log.e(TAG, "falha ao checar limite de categoria", it) }
                    }
                }
                .onFailure { Log.e(TAG, "falha ao ingerir notificação", it) }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "BankNotifListener"
    }
}
