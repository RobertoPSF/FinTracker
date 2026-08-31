package com.rfilh.fintracker.data.db

import androidx.room.TypeConverter
import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.LoanStatus
import com.rfilh.fintracker.domain.model.TransactionType

class Converters {
    @TypeConverter fun typeToString(v: TransactionType): String = v.name
    @TypeConverter fun stringToType(v: String): TransactionType =
        runCatching { TransactionType.valueOf(v) }.getOrDefault(TransactionType.UNKNOWN)

    @TypeConverter fun confToString(v: Confidence): String = v.name
    @TypeConverter fun stringToConf(v: String): Confidence =
        runCatching { Confidence.valueOf(v) }.getOrDefault(Confidence.LOW)

    @TypeConverter fun loanToString(v: LoanStatus): String = v.name
    @TypeConverter fun stringToLoan(v: String): LoanStatus =
        runCatching { LoanStatus.valueOf(v) }.getOrDefault(LoanStatus.NONE)
}
