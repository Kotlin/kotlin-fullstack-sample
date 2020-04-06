package org.jetbrains.demo.thinkter

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import org.jetbrains.demo.thinkter.dao.ThinkterDatabase

const val SESSION_NAME: String = "SESSION_NAME"

data class Session(val userId: String)

@KtorExperimentalLocationsAPI
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

    install(Sessions) {
        cookie<Session>(SESSION_NAME) {
            transform(SessionTransportTransformerMessageAuthentication(hashKey))
        }
    }

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
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

