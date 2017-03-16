package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

fun Route.delete(dao: ThinkterStorage, hashFunction: (String) -> String) {
    get<ThoughtDelete> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val date = System.currentTimeMillis()

        if (user == null) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val code = call.securityCode(date, user, hashFunction)
            call.respond(PostThoughtToken(user.userId, date, code))
        }
    }

    post<ThoughtDelete> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val thought = dao.getThought(it.id)

        if (user == null || thought.userId != user.userId || !call.verifyCode(it.date, user, it.code, hashFunction)) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            dao.deleteThought(it.id)
            call.respond(object : RpcData {})
        }
    }
}
