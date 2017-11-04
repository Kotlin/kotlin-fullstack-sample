package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.ValuesMap

fun Route.postThought(dao: ThinkterStorage, hashFunction: (String) -> String) {
    get<PostThought> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }

        if (user == null) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val date = System.currentTimeMillis()
            val code = call.securityCode(date, user, hashFunction)
            call.respond(PostThoughtToken(user.userId, date, code))
        }
    }

    post<PostThought> {
        val form = call.receive<ValuesMap>()
        val userId = form["userId"] ?: ""
        val date = form["date"]?.toLong() ?: -1
        val code = form["code"] ?: ""
        val text = form["text"] ?: ""
        val replyTo = form["replyTo"]?.toInt()

        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        if (user == null || !call.verifyCode(date, user, code, hashFunction)) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val id = dao.createThought(user.userId, text, replyTo)
            call.respond(PostThoughtResult(dao.getThought(id)))
        }
    }
}