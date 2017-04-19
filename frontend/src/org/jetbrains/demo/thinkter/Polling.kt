package org.jetbrains.demo.thinkter

import kotlin.browser.*
import kotlin.js.*
import kotlinx.coroutines.experimental.launch

class Polling(val period: Int = 20000) {
    private var timerId = 0
    var lastTime: Long = Date().getTime().toLong()
    var listeners: MutableList<(NewMessages) -> Unit> = ArrayList()

    fun start() {
        lastTime = Date().getTime().toLong()
        listeners.forEach { it(NewMessages.None) }

        if (timerId == 0) {
            stop()
            timerId = window.setInterval({ tick() }, period)
        }
    }

    fun stop() {
        if (timerId > 0) {
            window.clearInterval(timerId)
            timerId = 0
        }
    }

    fun tick() {
        launch {
            val newMessagesText = pollFromLastTime(lastTime.toString())
            val newMessages = when {
                newMessagesText == "0" || newMessagesText.isBlank() -> NewMessages.None
                newMessagesText.endsWith("+") -> NewMessages.MoreThan(newMessagesText.removeSuffix("+").toInt())
                else -> NewMessages.Few(newMessagesText.toInt())
            }
            listeners.forEach { it(newMessages) }
        }
    }

    sealed class NewMessages {
        object None : NewMessages()
        data class Few(val n: Int) : NewMessages()
        data class MoreThan(val n: Int) : NewMessages()
    }
}
