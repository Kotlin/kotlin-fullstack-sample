package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.html.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

fun Route.index(storage: ThinkterStorage) {
    contentType(ContentType.Text.Html) {
        get<Index> {
            call.respondHtmlTemplate(ApplicationPage()) {
                caption { +"Thinkter" }
            }
        }
    }
    contentType(ContentType.Application.Json) {
        get<Index> {
            val user = call.sessionOrNull<Session>()?.let { storage.user(it.userId) }
            val top = storage.top(10).map { storage.getThought(it) }
            val latest = storage.latest(10).map { storage.getThought(it) }
            val etagString = user?.userId + "," + top.joinToString { it.id.toString() } + latest.joinToString { it.id.toString() }
            val etag = etagString.hashCode()
        }
    }
}
