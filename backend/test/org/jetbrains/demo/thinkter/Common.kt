package org.jetbrains.demo.thinkter

import io.mockk.every
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.User
import org.jetbrains.ktor.application.ApplicationCall

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
