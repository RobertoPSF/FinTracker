package com.rfilh.fintracker.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.rfilh.fintracker.FinTrackerApp
import com.rfilh.fintracker.core.TextNormalizer
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

    /**
     * O sistema desvincula o listener periodicamente (mais agressivo em Samsung/Xiaomi com
     * economia de bateria) — sem isso, `NotificationAccess.rebind()` nunca era chamado e o
     * app parava de capturar notificações silenciosamente até o usuário desligar/religar a
     * permissão manualmente.
     */
    override fun onListenerDisconnected() {
        Log.w(TAG, "listener desvinculado pelo sistema, solicitando rebind")
        NotificationAccess.rebind(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        // Calculado cedo: usado tanto pra decidir se processa apps sem cadastro (mais abaixo)
        // quanto pra decidir se dispara notificação de debug em cada ponto de anomalia — bancos
        // já cadastrados deveriam SEMPRE virar transação, então qualquer desvio aqui é bug real.
        val hasOverride = container.rules().hasOverride(pkg)

        // Alguns bancos (ex. BB) marcam a própria notificação do Pix como resumo de grupo —
        // descartar FLAG_GROUP_SUMMARY sem olhar o conteúdo perdia essas notificações por
        // completo. A dedup por hash/similaridade no ingestor já cobre o caso normal (resumo +
        // notificação-filha com o mesmo conteúdo), então não precisamos filtrar aqui.
        val extras = sbn.notification.extras
        if (extras == null) {
            Log.w(TAG, "$pkg: notificação sem extras")
            if (hasOverride) DebugNotifier.noExtras(applicationContext, sbn)
            return
        }
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                ?.joinToString("\n") { it.toString() }
        if (title.isNullOrBlank() && text.isNullOrBlank()) {
            Log.w(TAG, "$pkg: sem título/texto extraível (extras: ${extras.keySet().joinToString()})")
            if (hasOverride) DebugNotifier.noExtractableText(applicationContext, sbn, extras)
            return
        }

        // App já com override cadastrado em parser_rules.json: sempre processa. App desconhecido
        // (fintech nova, banco pequeno, o que vier a existir): só processa se o texto claramente
        // fala de Pix — evita varrer notificação de qualquer app instalado, mas não exige
        // cadastro prévio pra capturar um banco que ainda nem existia quando o app foi lançado.
        if (!hasOverride && !looksLikePix(title, text)) return

        // Sem override, usa o nome de exibição do próprio Android em vez do package cru
        // (ex. "Nova Fintech" em vez de "com.novafintech.app").
        val appLabel = if (hasOverride) null else resolveAppLabel(pkg)
        if (!hasOverride) Log.d(TAG, "$pkg ($appLabel): app desconhecido, mas parece Pix — tentando classificar")

        val incoming = NotificationIngestor.Incoming(
            packageName = pkg,
            title = title,
            body = text,
            postedAt = if (sbn.postTime > 0) sbn.postTime else System.currentTimeMillis(),
            appLabel = appLabel
        )

        scope.launch {
            runCatching { container.ingestor.ingest(incoming) }
                .onSuccess { outcome ->
                    Log.d(TAG, "$pkg: ${outcome::class.simpleName}")
                    if (hasOverride && outcome is NotificationIngestor.Outcome.Ignored) {
                        DebugNotifier.ignoredByClassifier(applicationContext, pkg, title, text)
                    }
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

    /** Palavra isolada "pix" (sem acento, minúscula) — evita falso positivo tipo "pixel"/"Pixar". */
    private fun looksLikePix(title: String?, text: String?): Boolean {
        val combined = TextNormalizer.normalize(listOfNotNull(title, text).joinToString(" "))
        return PIX_WORD.containsMatchIn(combined)
    }

    private fun resolveAppLabel(pkg: String): String? = runCatching {
        val info = packageManager.getApplicationInfo(pkg, 0)
        packageManager.getApplicationLabel(info).toString()
    }.getOrNull()

    companion object {
        private const val TAG = "BankNotifListener"
        private val PIX_WORD = Regex("\\bpix\\b")
    }
}
