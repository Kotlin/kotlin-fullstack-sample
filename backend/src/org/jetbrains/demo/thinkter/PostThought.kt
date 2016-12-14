package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

fun Route.postThought(dao: ThinkterStorage, hashFunction: (String) -> String) {
    get<PostThought> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }

        if (user == null) {
            call.redirect(Login())
        } else {
            val date = System.currentTimeMillis()
            val code = call.securityCode(date, user, hashFunction)
            TODO()

            //call.respond(FreeMarkerContent("new-kweet.ftl", mapOf("user" to user, "date" to date, "code" to code), user.userId))
        }
    }
    post<PostThought> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        if (user == null || !call.verifyCode(it.date, user, it.code, hashFunction)) {
            call.redirect(Login())
        } else {
            val id = dao.createThought(user.userId, it.text, null)
            call.redirect(ViewThought(id))
        }
    }
}