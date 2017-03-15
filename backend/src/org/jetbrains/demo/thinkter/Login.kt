package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

fun Route.login(dao: ThinkterStorage, hash: (String) -> String) {
    get<Login> {
        call.respond(HttpStatusCode.MethodNotAllowed)
    }
    post<Login> {
        val login = when {
            it.userId.length < 4 -> null
            it.password.length < 6 -> null
            !userNameValid(it.userId) -> null
            else -> dao.user(it.userId, hash(it.password))
        }

        if (login == null) {
            call.respond(LoginResponse(error = "Invalid username or password"))
        } else {
            call.session(Session(login.userId))
            call.respond(LoginResponse(login))
        }
    }
    post<Logout> {
        call.clearSession()
        call.respond(HttpStatusCode.OK)
    }
}
