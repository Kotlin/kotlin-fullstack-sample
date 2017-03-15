package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

fun Route.delete(dao: ThinkterStorage, hashFunction: (String) -> String) {
    post<ThoughtDelete> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val thought = dao.getThought(it.id)

        if (user == null || thought.userId != user.userId || !call.verifyCode(it.date, user, it.code, hashFunction)) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            dao.deleteThought(it.id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
