package org.jetbrains.demo.thinkter

import io.mockk.*
import io.mockk.junit.MockKJUnit4Runner
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.PostThoughtToken
import org.jetbrains.ktor.http.HttpHeaders
import org.jetbrains.ktor.http.HttpMethod
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

    val getPostThought = DslRouteSlot()
    val postPostThought = DslRouteSlot()

    @Before
    fun setUp() {
        route.mockDsl(locations) {
            mockObj<PostThought> {
                mockSelect(HttpMethodRouteSelector(HttpMethod.Get)) {
                    captureHandle(getPostThought)
                }
                mockSelect(HttpMethodRouteSelector(HttpMethod.Post)) {
                    captureHandle(postPostThought)
                }
            }
        }

        route.postThought(dao, hash)
    }

    @Test
    fun testGetPostThought() {
        getPostThought.issueCall(locations, PostThought()) { handle ->
            mockSessionReturningUser(dao)

            every { request.host() } returns "host"

            every { request.headers[HttpHeaders.Referrer] } returns "abc"

            every { hash.hint(String::class).invoke(any()) } answers { firstArg<String>().reversed() }

            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                respond(assert<PostThoughtToken> {
                    it!!.user == "userId" &&
                            it.code.contains("llun:tsoh:dIresu")
                })
            }
        }
    }

}