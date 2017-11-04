package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.sessions.*

fun Route.viewThought(dao: ThinkterStorage, hashFunction: (String) -> String) {
    get<ViewThought> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        val date = System.currentTimeMillis()
        val code = if (user != null) call.securityCode(date, user, hashFunction) else null

        call.respond(ViewThoughtResponse(dao.getThought(it.id), date, code))
    }
}

