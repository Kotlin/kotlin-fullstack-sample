package org.jetbrains.demo.thinkter

import io.mockk.*
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.Thought
import org.jetbrains.demo.thinkter.model.User
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.HttpHeaders
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.request.host
import org.jetbrains.ktor.sessions.SessionConfig
import org.jetbrains.ktor.util.AttributeKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun MockKMatcherScope.sessionMatcher(): AttributeKey<Session> =
        match({ it!!.name == "Session" })

fun MockKMatcherScope.sessionConfigMatcher(): AttributeKey<SessionConfig<*>> =
        match({ it!!.name == "SessionConfig" })


fun ApplicationCall.mockSessionReturningUser(dao: ThinkterStorage) {
    every { attributes.contains(sessionMatcher()) } returns true

    every {
        attributes
                .hint(Session::class)
                .get(sessionMatcher())
    } returns Session("userId")

    every { dao.user("userId") } returns User("userId",
            "email",
            "User",
            "pwd")
}


fun ApplicationCall.mockSessionReturningNothing() {
    every { attributes.contains(sessionMatcher()) } returns false
}


fun ApplicationCall.checkForbiddenIfSesionReturningNothing(handle: () -> Unit) {
    mockSessionReturningNothing()

    coEvery { respond(any()) } just Runs

    handle()

    coVerify { respond(HttpStatusCode.Forbidden) }
}

fun ApplicationCall.mockHostReferrerHash(hash: (String) -> String) {
    every { request.host() } returns "host"

    every { request.headers[HttpHeaders.Referrer] } returns "http://abc/referrer"

    every { hash.hint(String::class).invoke(any()) } answers { firstArg<String>().reversed() }
}


fun mockGetThought(dao: ThinkterStorage, ts: Long) {
    every {
        dao.getThought(any())
    } answers { Thought(firstArg(),
            "userId",
            "text",
            formatDate(ts + firstArg<Int>()),
            null) }
}

private fun formatDate(date: Long): String {
    return Instant.ofEpochMilli(date)
            .atZone(ZoneId.systemDefault())
            .toOffsetDateTime()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}


fun mockUser(dao: ThinkterStorage, pwdHash: String? = null): User {
    val user = User("abcdef", "abc@def", "Abc Def", pwdHash ?: "")
    every { dao.user("abcdef", pwdHash) } returns user
    return user
}

fun ApplicationCall.mockPutSession() {
    every {
        attributes
                .hint(SessionConfig::class)
                .get(sessionConfigMatcher())
                .sessionType
    } returns Session::class

    every { attributes.put(sessionMatcher(), any()) } just Runs
}

