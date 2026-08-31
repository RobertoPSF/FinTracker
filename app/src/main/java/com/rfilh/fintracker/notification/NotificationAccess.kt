package com.rfilh.fintracker.notification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings

object NotificationAccess {

    fun isEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners"
        ).orEmpty()
        val component = ComponentName(context, BankNotificationListener::class.java)
        return enabled.split(":").any {
            val parsed = ComponentName.unflattenFromString(it)
            parsed != null && parsed.packageName == context.packageName &&
                parsed.className == component.className
        }
    }

    fun settingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** Força rebind quando o serviço fica ocioso (Android 7+ mata listeners). */
    fun rebind(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.service.notification.NotificationListenerService.requestRebind(
                ComponentName(context, BankNotificationListener::class.java)
            )
        }
    }
}
