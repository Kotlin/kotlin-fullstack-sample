package org.jetbrains.demo.thinkter

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import org.jetbrains.demo.thinkter.dao.ThinkterDatabase

data class Session(val userId: String)

fun Application.main() {
    val storage = ThinkterDatabase(/*JDBCConnection.Companion.create(H2Dialect, pool)*/)

    install(DefaultHeaders)
    install(CallLogging)
    install(ConditionalHeaders)
    install(PartialContent)
    install(Compression)
    install(Locations)
    install(StatusPages) {
        exception<NotImplementedError> { call.respond(HttpStatusCode.NotImplemented, "${it.message}") }
    }
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Sessions) {
        cookie<Session>(name = "SESSION") {
            transform(SessionTransportTransformerMessageAuthentication(hashKey))
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

