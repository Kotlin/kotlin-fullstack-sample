package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

fun Route.postThought(dao: ThinkterStorage, hashFunction: (String) -> String) {
    get<PostThought> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }

        if (user == null) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val date = System.currentTimeMillis()
            val code = call.securityCode(date, user, hashFunction)
            call.respond(PostThoughtToken(user.userId, date, code))
        }
    }
    post<PostThought> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        if (user == null || !call.verifyCode(it.date, user, it.code, hashFunction)) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val id = dao.createThought(user.userId, it.text, it.replyTo)
            call.respond(PostThoughtResult(dao.getThought(id)))
        }
    }
}