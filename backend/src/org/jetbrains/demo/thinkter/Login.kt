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

fun Route.login(dao: ThinkterStorage, hash: (String) -> String) {
    get<Login> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        if (user == null) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            call.respond(LoginResponse(user))
        }
    }
    post<Login> {
        val form = call.receive<Parameters>()
        val userId = form["userId"] ?: ""
        val password = form["password"] ?: ""

        val login = when {
            userId.length < 4 -> null
            password.length < 6 -> null
            !userNameValid(userId) -> null
            else -> dao.user(userId, hash(password))
        }

        if (login == null) {
            call.respond(LoginResponse(error = "Invalid username or password"))
        } else {
            call.sessions.set(Session(login.userId))
            call.respond(LoginResponse(login))
        }
    }
    post<Logout> {
        call.sessions.clear<Session>()
        call.respond(HttpStatusCode.OK)
    }
}
