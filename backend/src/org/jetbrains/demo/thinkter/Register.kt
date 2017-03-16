package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*
import org.jetbrains.ktor.util.*

fun Route.register(dao: ThinkterStorage, hashFunction: (String) -> String) {
    post<Register> { form ->
        val vm = call.request.content.get<ValuesMap>()

        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        if (user != null) {
            call.redirect(LoginResponse(user))
        } else {
            if (form.password.length < 6) {
                call.respond(LoginResponse(error = "Password should be at least 6 characters long"))
            } else if (form.userId.length < 4) {
                call.respond(LoginResponse(error = "Login should be at least 4 characters long"))
            } else if (!userNameValid(form.userId)) {
                call.respond(LoginResponse(error = "Login should be consists of digits, letters, dots or underscores"))
            } else if (dao.user(form.userId) != null) {
                call.respond(LoginResponse(error = "User with the following login is already registered"))
            } else {
                val hash = hashFunction(form.password)
                val newUser = User(form.userId, form.email, form.displayName, hash)

                try {
                    dao.createUser(newUser)
                } catch (e: Throwable) {
                    if (dao.user(form.userId) != null) {
                        call.respond(LoginResponse(error = "User with the following login is already registered"))
                    } else if (dao.userByEmail(form.email) != null) {
                        call.respond(LoginResponse(error = "User with the following email ${form.email} is already registered"))
                    } else {
                        application.environment.log.error("Failed to register user", e)
                        call.respond(LoginResponse(error = "Failed to register"))
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
