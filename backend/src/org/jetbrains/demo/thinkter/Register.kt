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

fun Route.register(dao: ThinkterStorage, hashFunction: (String) -> String) {
    post<Register> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        if (user != null) {
            call.redirect(LoginResponse(user))
        } else {
            val form = call.receive<Parameters>()
            val userId = form["userId"] ?: ""
            val email = form["email"] ?: ""
            val displayName = form["displayName"] ?: ""
            val password = form["password"] ?: ""

            if (password.length < 6) {
                call.respond(LoginResponse(error = "Password should be at least 6 characters long"))
            } else if (userId.length < 4) {
                call.respond(LoginResponse(error = "Login should be at least 4 characters long"))
            } else if (!userNameValid(userId)) {
                call.respond(LoginResponse(error = "Login should be consists of digits, letters, dots or underscores"))
            } else if (dao.user(userId) != null) {
                call.respond(LoginResponse(error = "User with the following login is already registered"))
            } else {
                val hash = hashFunction(password)
                val newUser = User(userId, email, displayName, hash)

                try {
                    dao.createUser(newUser)
                } catch (e: Throwable) {
                    if (dao.user(userId) != null) {
                        call.respond(LoginResponse(error = "User with the following login is already registered"))
                    } else if (dao.userByEmail(email) != null) {
                        call.respond(LoginResponse(error = "User with the following email $email is already registered"))
                    } else {
                        application.environment.log.error("Failed to register user", e)
                        call.respond(LoginResponse(error = "Failed to register"))
                    }
                }

                call.sessions.set(Session(newUser.userId))
                call.respond(LoginResponse(newUser))
            }
        }
    }
    get<Register> {
        call.respond(HttpStatusCode.MethodNotAllowed)
    }
}
