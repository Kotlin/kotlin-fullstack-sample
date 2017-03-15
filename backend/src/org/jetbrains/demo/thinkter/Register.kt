package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

fun Route.register(dao: ThinkterStorage, hashFunction: (String) -> String) {
    post<Register> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        if (user != null) {
            call.redirect(LoginResponse(user))
        } else {
            if (it.password.length < 6) {
                call.respond(LoginResponse(error = "Password should be at least 6 characters long"))
            } else if (it.userId.length < 4) {
                call.respond(LoginResponse(error = "Login should be at least 4 characters long"))
            } else if (!userNameValid(it.userId)) {
                call.respond(LoginResponse(error = "Login should be consists of digits, letters, dots or underscores"))
            } else if (dao.user(it.userId) != null) {
                call.respond(LoginResponse(error = "User with the following login is already registered"))
            } else {
                val hash = hashFunction(it.password)
                val newUser = User(it.userId, it.email, it.displayName, hash)

                try {
                    dao.createUser(newUser)
                } catch (e: Throwable) {
                    if (dao.user(it.userId) != null) {
                        call.redirect(it.copy(error = "User with the following login is already registered", password = ""))
                    } else if (dao.userByEmail(it.email) != null) {
                        call.redirect(it.copy(error = "User with the following email ${it.email} is already registered", password = ""))
                    } else {
                        application.environment.log.error("Failed to register user", e)
                        call.redirect(it.copy(error = "Failed to register", password = ""))
                    }
                }

                call.session(Session(newUser.userId))
                call.respond(LoginResponse(newUser))
            }
        }
    }
    get<Register> {
        call.respond(HttpStatusCode.MethodNotAllowed)
    }
}
