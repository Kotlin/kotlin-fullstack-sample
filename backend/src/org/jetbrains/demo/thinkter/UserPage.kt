package org.jetbrains.demo.thinkter

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.UserThoughtsResponse

@KtorExperimentalLocationsAPI
fun Route.userPage(dao: ThinkterStorage) {
    get<UserThoughts> {
        val viewUser = dao.user(it.user)

        if (viewUser == null) {
            call.respond(HttpStatusCode.NotFound.description("User ${it.user} doesn't exist"))
        } else {
            val thoughts = dao.userThoughts(it.user).map { dao.getThought(it) }
            call.respond(UserThoughtsResponse(viewUser, thoughts))
        }
    }
}
