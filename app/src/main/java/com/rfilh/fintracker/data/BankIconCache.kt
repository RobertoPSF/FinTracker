package com.rfilh.fintracker.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import java.io.File

/**
 * Cacheia em disco o ícone do app de cada banco/fintech que já mandou notificação — usado pra
 * exibir o ícone real na UI em vez de só cor+inicial. Vem do launcher (colorido, estável) e não
 * do ícone da notificação em si, que por convenção do Android costuma ser um glifo monocromático
 * (fica ruim ampliado num avatar).
 */
object BankIconCache {
    private const val DIR = "bank_icons"
    private const val ICON_SIZE_PX = 128

    fun iconFile(context: Context, packageName: String): File =
        File(File(context.filesDir, DIR), "$packageName.png")

    /** Idempotente: não refaz o trabalho se já tiver cache. Silencioso em qualquer falha. */
    fun ensureCached(context: Context, packageName: String) {
        val file = iconFile(context, packageName)
        if (file.exists()) return
        runCatching {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            val bitmap = drawable.toBitmap(ICON_SIZE_PX)
            file.parentFile?.mkdirs()
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun Drawable.toBitmap(size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }
}
