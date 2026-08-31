package com.rfilh.fintracker

import android.content.Context
import com.rfilh.fintracker.data.db.AppDatabase
import com.rfilh.fintracker.data.db.DatabaseFactory
import com.rfilh.fintracker.data.repo.FinanceRepositoryImpl
import com.rfilh.fintracker.domain.repo.FinanceRepository
import com.rfilh.fintracker.notification.NotificationIngestor
import com.rfilh.fintracker.parser.ParserRules
import com.rfilh.fintracker.parser.RulesProvider
import com.rfilh.fintracker.parser.TransactionClassifier

/** DI manual: nada de framework para tão poucas dependências. */
class AppContainer(private val context: Context) {

    val db: AppDatabase by lazy { DatabaseFactory.get(context) }

    val repository: FinanceRepository by lazy {
        FinanceRepositoryImpl(db.transactionDao(), db.rawDao())
    }

    val ingestor: NotificationIngestor by lazy {
        NotificationIngestor(db.rawDao(), db.transactionDao()) { classifier() }
    }

    fun rules(): ParserRules = RulesProvider.get(context)

    fun classifier(): TransactionClassifier = TransactionClassifier(rules())
}
