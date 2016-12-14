package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.features.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.logging.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*
import java.io.*

data class Session(val userId: String)

fun Application.main() {
    val dir = File("build/db")
    val database = ThinkterDatabase(/*JDBCConnection.Companion.create(H2Dialect, pool)*/)
    val storage: ThinkterStorage = ThinkterCache(database, File(dir.parentFile, "ehcache"))

    install(DefaultHeaders)
    install(CallLogging)
    install(ConditionalHeaders)
    install(PartialContentSupport)
    install(Locations)
    install(StatusPages) {
        exception<NotImplementedError> { call.respond(HttpStatusCode.NotImplemented) }
    }

    withSessions<Session> {
        withCookieByValue {
            settings = SessionCookiesSettings(transformers = listOf(SessionCookieTransformerMessageAuthentication(hashKey)))
        }
    }

    routing {
        index(storage)
        postThought(storage, ::hash)
        delete(storage, ::hash)
        userPage(storage)
        viewThought(storage, ::hash)

        login(storage, ::hash)
        register(storage, ::hash)
    }
}

