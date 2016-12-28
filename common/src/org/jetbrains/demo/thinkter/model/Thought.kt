package org.jetbrains.demo.thinkter.model

data class Thought(val id: Int, val userId: String, val text: String, val date: String, val replyTo: Int?)