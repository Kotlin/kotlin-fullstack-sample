package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.respond
import io.ktor.routing.*

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
