package com.rfilh.fintracker.core

import java.security.MessageDigest

object Hashing {
    /** Dedup: mesma notificação reentregue muda postTime em ms; janela de 60s. */
    fun notificationHash(pkg: String, title: String?, body: String?, postedAt: Long): String {
        val bucket = postedAt / 60_000L
        return sha256("$pkg|${title.orEmpty()}|${body.orEmpty()}|$bucket")
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
