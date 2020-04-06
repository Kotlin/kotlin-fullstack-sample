package org.jetbrains.demo.thinkter

import io.ktor.application.application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.LoginResponse
import org.jetbrains.demo.thinkter.model.User

@KtorExperimentalLocationsAPI
fun Route.register(dao: ThinkterStorage, hashFunction: (String) -> String) {
    post<Register> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        if (user != null) {
            call.redirect(LoginResponse(user))
        } else {
            val parameters = call.receive<Parameters>()
            val form = Register(
                    userId = parameters["userId"] ?: "",
                    displayName = parameters["displayName"] ?: "",
                    email = parameters["email"] ?: "",
                    password = parameters["password"] ?: "",
                    error = parameters["error"] ?: ""
            )
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
                call.sessions.set(Session(newUser.userId))
                call.respond(LoginResponse(newUser))
            }
        }
    }
    get<Register> {
        call.respond(HttpStatusCode.MethodNotAllowed)
    }
}
