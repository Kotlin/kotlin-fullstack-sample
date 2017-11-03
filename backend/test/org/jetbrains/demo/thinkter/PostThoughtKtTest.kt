package org.jetbrains.demo.thinkter

import io.mockk.*
import io.mockk.junit.MockKJUnit4Runner
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.PostThoughtResult
import org.jetbrains.demo.thinkter.model.PostThoughtToken
import org.jetbrains.demo.thinkter.model.Thought
import org.jetbrains.ktor.http.HttpHeaders
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.request.host
import org.jetbrains.ktor.routing.HttpMethodRouteSelector
import org.jetbrains.ktor.routing.Routing
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(MockKJUnit4Runner::class)
class PostThoughtKtTest {
    val route = mockk<Routing>()
    val dao = mockk<ThinkterStorage>()
    val hash = mockk<(String) -> String>()
    val locations = mockk<Locations>()

    val getPostThought = RouteBlockSlot()
    val postPostThought = RouteBlockSlot()

    @Before
    fun setUp() {
        route.mockDsl(locations) {
            mockObj<PostThought> {
                mockSelect(HttpMethodRouteSelector(HttpMethod.Get)) {
                    captureBlock(getPostThought)
                }
                mockSelect(HttpMethodRouteSelector(HttpMethod.Post)) {
                    captureBlock(postPostThought)
                }
            }
        }

        route.postThought(dao, hash)
    }

    @Test
    fun testGetPostThoughtOk() {
        getPostThought.invokeBlock(locations, PostThought()) { handle ->
            mockSessionReturningUser(dao)

            every { request.host() } returns "host"

            every { request.headers[HttpHeaders.Referrer] } returns "http://abc/referrer"

            every { hash.hint(String::class).invoke(any()) } answers { firstArg<String>().reversed() }

            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                respond(assert<PostThoughtToken> {
                    it!!.user == "userId" &&
                            it.code.contains("cba:tsoh:dIresu")
                })
            }
        }
    }

    @Test
    fun testGetPostThoughtForbidden() {
        getPostThought.invokeBlock(locations, PostThought()) { handle ->
            mockSessionReturningNothing()

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(HttpStatusCode.Forbidden) }
        }
    }

    @Test
    fun testPostPostThoughtOk() {
        val ts = System.currentTimeMillis() - 6000
        val data = PostThought("text", ts, "cba:tsoh:dIresu:" + ts.toString().reversed(), null)
        postPostThought.invokeBlock(locations, data) { handle ->
            mockSessionReturningUser(dao)

            every { request.host() } returns "host"

            every { request.headers[HttpHeaders.Referrer] } returns "http://abc/referrer"

            every { hash.hint(String::class).invoke(any()) } answers { firstArg<String>().reversed() }

            every {
                dao.createThought("userId", "text", any(), any())
            } returns 1

            every {
                dao.getThought(1)
            } answers { Thought(1, "userId", "text", ts.toString(), null) }

            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                respond(assert<PostThoughtResult> {
                    it!!.thought.id == 1 &&
                            it.thought.text == "text"
                })
            }
        }
    }

    @Test
    fun testPostPostThoughtForbidden() {
        postPostThought.invokeBlock(locations, PostThought()) { handle ->
            mockSessionReturningNothing()

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(HttpStatusCode.Forbidden) }
        }
    }
}