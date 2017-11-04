package org.jetbrains.demo.thinkter

import assertk.assertions.contains
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.cio.ByteBufferWriteChannel
import org.jetbrains.ktor.cio.toOutputStream
import org.jetbrains.ktor.html.HtmlContent
import org.jetbrains.ktor.html.respondHtmlTemplate
import org.junit.Assert.*
import org.junit.Test
import java.nio.charset.Charset

class ApplicationPageTest {
    val appCall = mockk<ApplicationCall>()

    @Test
    fun testRenderHTML() {
        coEvery { appCall.respond(any()) } just Runs

        runBlocking {
            appCall.respondHtmlTemplate(ApplicationPage()) {
                caption { +"caption" }
            }
        }

        val channel = ByteBufferWriteChannel()
        coVerify {
            appCall.respond(coAny<HtmlContent> {
                it!!.writeTo(channel)
            })
        }

        val generatedPage = channel.toString(Charset.defaultCharset())

        assertk.assert(generatedPage)
                .contains("caption")
        assertk.assert(generatedPage)
                .contains("yui.yahooapis.com")
    }
}