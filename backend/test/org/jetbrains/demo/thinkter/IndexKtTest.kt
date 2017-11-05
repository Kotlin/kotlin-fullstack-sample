package org.jetbrains.demo.thinkter

import io.mockk.*
import io.mockk.junit.MockKJUnit4Runner
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.IndexResponse
import org.jetbrains.demo.thinkter.model.PollResponse
import org.jetbrains.ktor.cio.ByteBufferWriteChannel
import org.jetbrains.ktor.html.HtmlContent
import org.jetbrains.ktor.http.HttpHeaders
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.routing.HttpHeaderRouteSelector
import org.jetbrains.ktor.routing.HttpMethodRouteSelector
import org.jetbrains.ktor.routing.Routing
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.Charset

@RunWith(MockKJUnit4Runner::class)
class IndexKtTest {
    val route = mockk<Routing>()
    val dao = mockk<ThinkterStorage>()
    val locations = mockk<Locations>()

    val getHtmlIndex = RouteBlockSlot()
    val getJsonIndex = RouteBlockSlot()
    val getJsonPoll = RouteBlockSlot()

    @Before
    fun setUp() {
        route.mockDsl(locations) {
            mockSelect(HttpHeaderRouteSelector(HttpHeaders.Accept, "text/html")) {
                mockObj<Index> {
                    mockSelect(HttpMethodRouteSelector(HttpMethod.Get)) {
                        captureBlock(getHtmlIndex)
                    }
                }
            }
            mockSelect(HttpHeaderRouteSelector(HttpHeaders.Accept, "application/json")) {
                mockObj<Index> {
                    mockSelect(HttpMethodRouteSelector(HttpMethod.Get)) {
                        captureBlock(getJsonIndex)
                    }
                }
                mockObj<Poll> {
                    mockSelect(HttpMethodRouteSelector(HttpMethod.Get)) {
                        captureBlock(getJsonPoll)
                    }
                }
            }
        }

        route.index(dao)

    }

    @Test
    fun testGetIndexHtml() {
        getHtmlIndex.invokeBlock(locations, Index()) { handle ->
            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                respond(coAssert<HtmlContent> {
                    val channel = ByteBufferWriteChannel()
                    it.writeTo(channel)
                    val html = channel.toString(Charset.defaultCharset())
                    html.contains("<title>Thinkter</title>")
                })
            }
        }
    }

    @Test
    fun testGetIndexJson() {
        getJsonIndex.invokeBlock(locations, Index()) { handle ->
            mockSessionReturningUser(dao)
            mockGetThought(dao, 0)

            every { dao.top(10) } returns (1..10).toList()

            every { dao.latest(10) } returns (1..10).toList()

            coEvery { respond(any()) } just Runs

            every { response.pipeline.intercept(any(), any()) } just Runs

            handle()

            coVerify {
                respond(assert<IndexResponse>(msg = "response should have top and latest with ids from one to ten") {
                    val oneToTen = (1..10).toList()

                    it.top.map { it.id }.containsAll(oneToTen)
                            && it.latest.map { it.id }.containsAll(oneToTen)
                })
            }
        }
    }

    @Test
    fun testGetPollJsonBlank() {
        getJsonPoll.invokeBlock(locations, Poll("")) { handle ->
            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                respond(assert<PollResponse> { it.count == "0" })
            }
        }
    }

    @Test
    fun testGetPollJsonOne() {
        checkPoll("9", "1")
    }

    @Test
    fun testGetPollJsonFive() {
        checkPoll("5", "5")
    }

    @Test
    fun testGetPollJsonTenPlus() {
        checkPoll("0", "10+")
    }

    private fun checkPoll(pollTime: String, responseCount: String) {
        getJsonPoll.invokeBlock(locations, Poll(pollTime)) { handle ->
            mockGetThought(dao, 0)

            every { dao.latest(10) } returns (1..10).toList()

            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                respond(assert<PollResponse> { it.count == responseCount })
            }
        }
    }

}

