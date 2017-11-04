package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.contentType
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import java.time.*

fun Route.index(storage: ThinkterStorage) {
    get<Index> {
        val user = call.sessions.get<Session>()?.let { storage.user(it.userId) }
        val top = storage.top(10).map(storage::getThought)
        val latest = storage.latest(10).map(storage::getThought)

        if(call.request.contentType().match(ContentType.Application.Json)) {
            call.response.pipeline.intercept(ApplicationSendPipeline.After) {
                val etagString = user?.userId + "," + top.joinToString { it.id.toString() } + latest.joinToString { it.id.toString() }
                call.response.etag(etagString)
            }

            call.respond(IndexResponse(top, latest))
        }else{
            call.respondHtmlTemplate(ApplicationPage()) {
                caption { +"Thinkter" }
            }
        }
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

private fun Thought.toEpochMilli() = LocalDateTime.parse(date).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
