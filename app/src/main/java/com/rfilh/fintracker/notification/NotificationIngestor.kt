package com.rfilh.fintracker.notification

import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.Hashing
import com.rfilh.fintracker.data.db.RawNotificationDao
import com.rfilh.fintracker.data.db.RawNotificationEntity
import com.rfilh.fintracker.data.db.TransactionDao
import com.rfilh.fintracker.data.db.TransactionEntity
import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.TransactionType
import com.rfilh.fintracker.parser.ParseResult
import com.rfilh.fintracker.parser.TransactionClassifier

/** Pipeline: notificação bruta -> dedup -> persistência -> classificação -> transação. */
class NotificationIngestor(
    private val rawDao: RawNotificationDao,
    private val transactionDao: TransactionDao,
    private val classifierProvider: () -> TransactionClassifier,
    private val autoPurgeRaw: () -> Boolean = { false }
) {

    data class Incoming(
        val packageName: String,
        val title: String?,
        val body: String?,
        val postedAt: Long
    )

    sealed interface Outcome {
        data object Duplicate : Outcome
        data object Ignored : Outcome
        data class Stored(
            val transactionId: Long,
            val type: TransactionType,
            val category: String?,
            val yearMonth: String
        ) : Outcome
    }

    suspend fun ingest(incoming: Incoming): Outcome {
        val hash = Hashing.notificationHash(
            incoming.packageName, incoming.title, incoming.body, incoming.postedAt
        )

        val rawId = rawDao.insert(
            RawNotificationEntity(
                packageName = incoming.packageName,
                title = incoming.title,
                body = incoming.body,
                postedAt = incoming.postedAt,
                hash = hash
            )
        )
        if (rawId == -1L) return Outcome.Duplicate

        val result = classifierProvider().classify(incoming.packageName, incoming.title, incoming.body)
        if (result !is ParseResult.Parsed) {
            rawDao.markParsed(rawId)
            if (autoPurgeRaw()) rawDao.purgeOne(rawId)
            return Outcome.Ignored
        }

        val entity = TransactionEntity(
            bankPackage = incoming.packageName,
            bankName = result.bankName,
            type = result.type,
            amountCents = result.amountCents ?: 0L,
            counterparty = result.counterparty,
            occurredAt = incoming.postedAt,
            yearMonth = Dates.yearMonth(incoming.postedAt),
            confidence = if (result.amountCents == null) Confidence.LOW else result.confidence,
            reviewed = false,
            category = result.category,
            rawId = rawId
        )
        val id = transactionDao.insert(entity)
        rawDao.markParsed(rawId)
        if (autoPurgeRaw()) rawDao.purgeOne(rawId)
        return Outcome.Stored(id, entity.type, entity.category, entity.yearMonth)
    }
}
