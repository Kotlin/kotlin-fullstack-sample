package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*

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
