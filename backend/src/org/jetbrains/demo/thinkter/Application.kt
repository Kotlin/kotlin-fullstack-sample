package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.GsonConverter
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.sessions.*

data class Session(val userId: String)

fun Application.main() {
    val storage = ThinkterDatabase(/*JDBCConnection.Companion.create(H2Dialect, pool)*/)

    install(DefaultHeaders)
    install(CallLogging)
    install(ConditionalHeaders)
    install(PartialContentSupport)
    install(Compression)
    install(Locations)
    install(StatusPages) {
        exception<NotImplementedError> { call.respond(HttpStatusCode.NotImplemented) }
    }

    install(Sessions){
        cookie<Session>("SESSION"){
            transform(SessionTransportTransformerMessageAuthentication(hashKey))
        }
    }

    install(ContentNegotiation){
        register(ContentType.Application.Json, GsonConverter())
    }

    install(Routing) {
        index(storage)
        postThought(storage, ::hash)
        delete(storage, ::hash)
        userPage(storage)
        viewThought(storage, ::hash)

        login(storage, ::hash)
        register(storage, ::hash)
    }
}