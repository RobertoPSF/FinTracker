package com.rfilh.fintracker.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Auto-diagnóstico: quando um app já cadastrado em `parser_rules.json` (ex. Banco do Brasil)
 * tem alguma anomalia na captura — sem extras, sem título/texto extraível, ou capturado mas
 * ignorado pelo classificador — dispara uma notificação local mostrando exatamente o que
 * aconteceu. Não precisa de computador/adb pra investigar: aparece direto no aparelho.
 *
 * Só dispara pra apps com override cadastrado (bancos conhecidos): a expectativa é que esses
 * SEMPRE gerem uma transação a partir de um Pix, então qualquer anomalia aqui é sinal de bug
 * real, não ruído — diferente de um app desconhecido qualquer, onde "ignorado" é o caso comum.
 */
object DebugNotifier {
    private const val CHANNEL_ID = "capture_debug"
    private const val MAX_DUMP_LENGTH = 1200

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Diagnóstico de captura", NotificationManager.IMPORTANCE_LOW)
        )
    }

    fun noExtras(context: Context, sbn: StatusBarNotification) {
        notify(context, sbn.packageName, "sem extras", "flags=${sbn.notification.flags}")
    }

    fun noExtractableText(context: Context, sbn: StatusBarNotification, extras: android.os.Bundle) {
        val dump = extras.keySet().sorted().joinToString("\n") { key ->
            "$key = ${runCatching { extras.get(key)?.toString() }.getOrNull()}"
        }
        notify(context, sbn.packageName, "sem título/texto extraível", dump)
    }

    fun ignoredByClassifier(context: Context, packageName: String, title: String?, text: String?) {
        notify(context, packageName, "capturado mas ignorado pelo classificador", "título: $title\ntexto: $text")
    }

    private fun notify(context: Context, packageName: String, reason: String, detail: String) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val body = detail.take(MAX_DUMP_LENGTH)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("FinTracker [debug] $packageName: $reason")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(packageName.hashCode(), notification)
    }
}
