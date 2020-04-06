package org.jetbrains.demo.thinkter

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
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.PostThoughtResult
import org.jetbrains.demo.thinkter.model.PostThoughtToken

@KtorExperimentalLocationsAPI
fun Route.postThought(dao: ThinkterStorage, hashFunction: (String) -> String) {
    get<PostThought> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }

        if (user == null) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val date = System.currentTimeMillis()
            val code = call.securityCode(date, user, hashFunction)
            call.respond(PostThoughtToken(user.userId, date, code))
        }
    }
    post<PostThought> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        if (user == null) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val parameters = call.receive<Parameters>()
            val form = PostThought(
                    text = parameters["text"] ?: "",
                    date = (parameters["date"] ?: "0").toLong(),
                    code = parameters["code"] ?: ""
            )
            if (!call.verifyCode(form.date, user, form.code, hashFunction)) {
                call.respond(HttpStatusCode.Forbidden)
            } else {
                val id = dao.createThought(user.userId, form.text, form.replyTo)
                call.respond(PostThoughtResult(dao.getThought(id)))
            }
        }
    }
}