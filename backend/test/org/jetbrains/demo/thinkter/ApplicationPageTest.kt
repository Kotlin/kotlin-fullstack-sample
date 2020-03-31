package org.jetbrains.demo.thinkter

import io.mockk.*
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.cio.ByteBufferWriteChannel
import org.jetbrains.ktor.html.HtmlContent
import org.jetbrains.ktor.html.respondHtmlTemplate
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

        coVerify {
            appCall.respond(coAssert<HtmlContent> {
                val channel = ByteBufferWriteChannel()
                it.writeTo(channel)
                val html = channel.toString(Charset.defaultCharset())
                html.contains("caption") && html.contains("yui.yahooapis.com")
            })
        }

    }
}