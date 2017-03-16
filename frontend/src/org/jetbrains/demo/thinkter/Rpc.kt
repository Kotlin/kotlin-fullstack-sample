package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.model.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import kotlin.browser.*
import kotlin.js.*

fun index(): Promise<IndexResponse> {
    return getAndParseResult("/", null, ::parseIndexResponse)
}

fun register(userId: String, password: String, displayName: String, email: String): Promise<User> {
    return postAndParseResult("/register", URLSearchParams().apply {
        append("userId", userId)
        append("password", password)
        append("displayName", displayName)
        append("email", email)
    }, ::parseLoginOrRegisterResponse)
}

fun checkSession(): Promise<User> {
    return getAndParseResult("/login", null, ::parseLoginOrRegisterResponse)
}

fun login(userId: String, password: String): Promise<User> {
    return postAndParseResult("/login", URLSearchParams().apply {
        append("userId", userId)
        append("password", password)
    }, ::parseLoginOrRegisterResponse)
}

fun postThoughtPrepare(): Promise<PostThoughtToken> {
    return getAndParseResult("/post-new", null, ::parseNewPostTokenResponse)
}

fun postThought(replyTo: Int?, text: String, token: PostThoughtToken): Promise<Thought> {
    return postAndParseResult("/post-new", URLSearchParams().apply {
        append("text", text)
        append("date", token.date.toString())
        append("code", token.code)
        if (replyTo != null) {
            append("replyTo", replyTo.toString())
        }
    }, ::parsePostThoughtResponse)
}

fun logoutUser(): Promise<Unit> {
    return window.fetch("/logout", object: RequestInit {
        override var method: String? = "POST"
        override var credentials: RequestCredentials? = "same-origin".asDynamic()
    }).then({ Unit })
}

fun deleteThought(id: Int, date: Long, code: String): Promise<Unit> {
    return postAndParseResult("/thought/$id/delete", URLSearchParams().apply {
        append("date", date.toString())
        append("code", code)
    }, { Unit })
}

private fun parseIndexResponse(json: dynamic): IndexResponse {
    val top = json.top as Array<dynamic>
    val latest = json.latest as Array<dynamic>

    return IndexResponse(top.map(::parseThought), latest.map(::parseThought))
}

private fun parsePostThoughtResponse(json: dynamic): Thought {
    return parseThought(json.thought)
}

private fun parseThought(json: dynamic): Thought {
    return Thought(json.id, json.userId, json.text, json.date, json.replyTo)
}

private fun parseNewPostTokenResponse(json: dynamic): PostThoughtToken {
    return PostThoughtToken(json.user, json.date, json.code)
}

private fun parseLoginOrRegisterResponse(json: dynamic): User {
    if (json.error != null) {
        throw LoginOrRegisterFailedException(json.error.toString())
    }

    return User(json.user.userId, json.user.email, json.user.displayName, json.user.passwordHash)
}

class LoginOrRegisterFailedException(message: String) : Throwable(message)

fun <T> postAndParseResult(url: String, body: dynamic, parse: (dynamic) -> T): Promise<T> {
    return requestAndParseResult("POST", url, body, parse)
}

fun <T> getAndParseResult(url: String, body: dynamic, parse: (dynamic) -> T): Promise<T> {
    return requestAndParseResult("GET", url, body, parse)
}

fun <T> requestAndParseResult(method: String, url: String, body: dynamic, parse: (dynamic) -> T): Promise<T> {
    return Promise { resolve, reject ->
        window.fetch(url, object: RequestInit {
            override var method: String? = method
            override var body: dynamic = body
            override var credentials: RequestCredentials? = "same-origin".asDynamic()
            override var headers: dynamic = json("Accept" to "application/json")

        }).then({ response ->
            response.json().then({ resolve(parse(it)) }, reject).catch(reject)
        }, reject).catch(reject)
    }
}