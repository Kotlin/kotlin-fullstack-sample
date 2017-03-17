package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.html.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.response.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*
import java.time.*

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
            val top = storage.top(10).map(storage::getThought)
            val latest = storage.latest(10).map(storage::getThought)

            call.response.pipeline.intercept(ApplicationResponsePipeline.After) {
                val etagString = user?.userId + "," + top.joinToString { it.id.toString() } + latest.joinToString { it.id.toString() }
                call.response.etag(etagString)
            }

            call.respond(IndexResponse(top, latest))
        }
        get<Poll> { poll ->
            if (poll.lastTime.isBlank()) {
                call.respond(PollResponse(System.currentTimeMillis(), "0"))
            } else {
                val time = System.currentTimeMillis()
                val lastTime = poll.lastTime.toLong()

                val count = storage.latest(10).reversed().takeWhile { storage.getThought(it).toEpochMilli() > lastTime }.size

                call.respond(PollResponse(time, if (count == 10) "10+" else count.toString()))
            }
        }
    }
}

private fun Thought.toEpochMilli() = LocalDateTime.parse(date).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
