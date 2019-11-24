@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.jetbrains.demo.thinkter

import io.ktor.application.ApplicationCall
import io.ktor.application.feature
import io.ktor.http.HttpHeaders
import io.ktor.locations.Locations
import io.ktor.request.header
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.response.respondRedirect
import io.ktor.util.hex
import org.jetbrains.demo.thinkter.model.User
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

val hashKey = hex("6819b57a326945c1968f45236589")
val hmacKey = SecretKeySpec(hashKey, "HmacSHA1")
fun hash(password: String): String {
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}

fun ApplicationCall.refererHost() = request.header(HttpHeaders.Referrer)?.let { URI.create(it).host }

suspend fun ApplicationCall.redirect(location: Any) {
    val host = request.host()
    val portSpec = request.port().let { if (it == 80) "" else ":$it" }
    val address = host + portSpec

    respondRedirect("http://$address${application.feature(Locations).href(location)}")
}

fun ApplicationCall.securityCode(date: Long, user: User, hashFunction: (String) -> String) =
        hashFunction("$date:${user.userId}:${request.host()}:${refererHost()}")

fun ApplicationCall.verifyCode(date: Long, user: User, code: String, hashFunction: (String) -> String) =
        securityCode(date, user, hashFunction) == code
                && (System.currentTimeMillis() - date).let { it > 0 && it < TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS) }


private val userIdPattern = "[a-zA-Z0-9_.]+".toRegex()
internal fun userNameValid(userId: String) = userId.matches(userIdPattern)