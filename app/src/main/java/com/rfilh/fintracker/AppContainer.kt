package com.rfilh.fintracker

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import com.rfilh.fintracker.data.db.AppDatabase
import com.rfilh.fintracker.data.db.AppLockPrefs
import com.rfilh.fintracker.data.db.AppSettings
import com.rfilh.fintracker.data.db.DatabaseFactory
import com.rfilh.fintracker.data.repo.FinanceRepositoryImpl
import com.rfilh.fintracker.domain.repo.FinanceRepository
import com.rfilh.fintracker.notification.BudgetAlertNotifier
import com.rfilh.fintracker.notification.NotificationIngestor
import com.rfilh.fintracker.parser.ParserRules
import com.rfilh.fintracker.parser.RulesProvider
import com.rfilh.fintracker.parser.TransactionClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** DI manual: nada de framework para tão poucas dependências. */
class AppContainer(private val context: Context) {

    val db: AppDatabase by lazy { DatabaseFactory.get(context) }

    val repository: FinanceRepository by lazy {
        FinanceRepositoryImpl(db.transactionDao(), db.rawDao(), db.categoryBudgetDao(), db.loanPaymentDao())
    }

    val ingestor: NotificationIngestor by lazy {
        NotificationIngestor(db.rawDao(), db.transactionDao(), { classifier() }) {
            AppSettings.autoPurgeRaw(context)
        }
    }

    val budgetAlertNotifier: BudgetAlertNotifier by lazy {
        BudgetAlertNotifier(db.categoryBudgetDao(), db.transactionDao())
    }

    fun rules(): ParserRules = RulesProvider.get(context)

    fun classifier(): TransactionClassifier = TransactionClassifier(rules())

    fun isAppLockEnabled(): Boolean = AppLockPrefs.isEnabled(context)
    fun setAppLockEnabled(enabled: Boolean) = AppLockPrefs.setEnabled(context, enabled)

    fun canUseBiometricLock(): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL) ==
            BiometricManager.BIOMETRIC_SUCCESS

    fun isAutoPurgeRawEnabled(): Boolean = AppSettings.autoPurgeRaw(context)
    fun setAutoPurgeRawEnabled(enabled: Boolean) = AppSettings.setAutoPurgeRaw(context, enabled)

    fun savingsGoalCents(): Long = AppSettings.savingsGoalCents(context)
    fun setSavingsGoalCents(cents: Long) = AppSettings.setSavingsGoalCents(context, cents)

    private val _darkMode by lazy { MutableStateFlow(AppSettings.darkMode(context)) }
    val darkMode: StateFlow<Boolean> by lazy { _darkMode.asStateFlow() }

    fun setDarkMode(enabled: Boolean) {
        AppSettings.setDarkMode(context, enabled)
        _darkMode.value = enabled
    }
}
