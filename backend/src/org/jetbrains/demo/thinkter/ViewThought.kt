package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*

fun Route.viewThought(dao: ThinkterStorage, hashFunction: (String) -> String) {
    get<ViewThought> {
        val user = call.sessionOrNull<Session>()?.let { dao.user(it.userId) }
        val date = System.currentTimeMillis()
        val code = if (user != null) call.securityCode(date, user, hashFunction) else null
        TODO()

//        call.respond(FreeMarkerContent("view-kweet.ftl", mapOf("user" to user, "kweet" to dao.getThought(it.id), "date" to date, "code" to code), user?.userId ?: ""))
    }
}
