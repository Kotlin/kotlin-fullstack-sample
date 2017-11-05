package org.jetbrains.demo.thinkter

import io.mockk.*
import io.mockk.junit.MockKJUnit4Runner
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.PostThoughtResult
import org.jetbrains.demo.thinkter.model.PostThoughtToken
import org.jetbrains.demo.thinkter.model.Thought
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.routing.HttpMethodRouteSelector
import org.jetbrains.ktor.routing.Routing
import org.junit.Before
import org.junit.Ignore
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
            mockHostReferrerHash(hash)

            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                respond(assert<PostThoughtToken> {
                    it.user == "userId" &&
                            it.code.contains("cba:tsoh:dIresu")
                })
            }
        }
    }

    @Test
    fun testGetPostThoughtNotLoggedIn() {
        getPostThought.invokeBlock(locations, PostThought()) { handle ->
            checkForbiddenIfSesionReturningNothing(handle)
        }
    }

    @Test
    fun testPostPostThoughtOk() {
        val ts = System.currentTimeMillis() - 6000
        val data = PostThought("text", ts, "cba:tsoh:dIresu:" + ts.toString().reversed(), null)
        postPostThought.invokeBlock(locations, data) { handle ->
            mockSessionReturningUser(dao)
            mockHostReferrerHash(hash)
            mockGetThought(dao, ts)

            every {
                dao.createThought("userId", "text", any(), any())
            } returns 1


            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                respond(assert<PostThoughtResult> {
                    it.thought.id == 1 &&
                            it.thought.text == "text"
                })
            }
        }
    }

    @Test
    fun testPostPostThoughtNotLoggedIn() {
        postPostThought.invokeBlock(locations, PostThought()) { handle ->
            checkForbiddenIfSesionReturningNothing(handle)
        }
    }
}
