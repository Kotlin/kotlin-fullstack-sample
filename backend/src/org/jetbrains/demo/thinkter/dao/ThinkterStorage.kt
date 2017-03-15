package org.jetbrains.demo.thinkter.dao

import org.jetbrains.demo.thinkter.model.*
import java.io.*
import java.time.*

interface ThinkterStorage : Closeable {
    fun countReplies(id: Int): Int
    fun createThought(user: String, text: String, replyTo: Int? = null, date: LocalDateTime = LocalDateTime.now()): Int
    fun deleteThought(id: Int)
    fun getThought(id: Int): Thought
    fun userThoughts(userId: String): List<Int>
    fun user(userId: String, hash: String? = null): User?
    fun userByEmail(email: String): User?
    fun createUser(user: User)
    fun top(count: Long = 10): List<Int>
    fun latest(count: Long = 10): List<Int>
}


