package org.jetbrains.demo.thinkter

import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.request.*
import org.jetbrains.ktor.response.*
import org.jetbrains.ktor.util.*
import java.net.*
import java.util.concurrent.*
import javax.crypto.*
import javax.crypto.spec.*

val hashKey = hex("6819b57a326945c1968f45236589")
val hmacKey = SecretKeySpec(hashKey, "HmacSHA1")
fun hash(password: String): String {
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}

fun ApplicationCall.refererHost() = request.header(HttpHeaders.Referrer)?.let { URI.create(it).host }

suspend fun ApplicationCall.redirect(location: Any) {
    val host = request.host() ?: "localhost"
    val portSpec = request.port().let { if (it == 80) "" else ":$it" }
    val address = host + portSpec

    respondRedirect("http://$address${application.feature(Locations).href(location)}")
}

fun ApplicationCall.securityCode(date: Long, user: User, hashFunction: (String) -> String) =
        hashFunction("$date:${user.userId}:${request.host()}:${refererHost()}")

fun ApplicationCall.verifyCode(date: Long, user: User, code: String, hashFunction: (String) -> String) =
        securityCode(date, user, hashFunction) == code
                && (System.currentTimeMillis() - date).let { it > 0 && it < TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS) }


private val userIdPattern = "[a-zA-Z0-9_\\.]+".toRegex()
internal fun userNameValid(userId: String) = userId.matches(userIdPattern)