package org.jetbrains.demo.thinkter.model

import java.io.*
import java.time.*

data class Thought(val id: Int, val userId: String, val text: String, val date: LocalDateTime, val replyTo: Int?) : Serializable