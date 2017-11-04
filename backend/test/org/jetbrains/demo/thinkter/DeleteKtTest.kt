package org.jetbrains.demo.thinkter

import io.mockk.*
import io.mockk.junit.MockKJUnit4Runner
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.PostThoughtResult
import org.jetbrains.demo.thinkter.model.PostThoughtToken
import org.jetbrains.demo.thinkter.model.RpcData
import org.jetbrains.demo.thinkter.model.Thought
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.routing.HttpMethodRouteSelector
import org.jetbrains.ktor.routing.Routing
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(MockKJUnit4Runner::class)
class DeleteKtTest {
    val route = mockk<Routing>()
    val dao = mockk<ThinkterStorage>()
    val hash = mockk<(String) -> String>()
    val locations = mockk<Locations>()

    val getThoughtDelete = RouteBlockSlot()
    val postThoughtDelete = RouteBlockSlot()

    @Before
    fun setUp() {
        route.mockDsl(locations) {
            mockObj<ThoughtDelete> {
                mockSelect(HttpMethodRouteSelector(HttpMethod.Get)) {
                    captureBlock(getThoughtDelete)
                }
                mockSelect(HttpMethodRouteSelector(HttpMethod.Post)) {
                    captureBlock(postThoughtDelete)
                }
            }
        }

        route.delete(dao, hash)
    }

    @Test
    fun testGetThoughtDeleteOk() {
        val data = ThoughtDelete(1, System.currentTimeMillis() - 6000, "abc")
        getThoughtDelete.invokeBlock(locations, data) { handle ->
            mockSessionReturningUser(dao)
            mockHostReferrerHash(hash)

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
    fun testGetThoughtDeleteForbidden() {
        val data = ThoughtDelete(0, 0, "abc")
        getThoughtDelete.invokeBlock(locations, data) { handle ->
            checkForbiddenIfSesionReturningNothing(handle)
        }
    }

    @Test
    fun testPostThoughtDeleteOk() {
        val ts = System.currentTimeMillis() - 6000
        val data = ThoughtDelete(1, ts, "cba:tsoh:dIresu:" + ts.toString().reversed())
        postThoughtDelete.invokeBlock(locations, data) { handle ->
            mockSessionReturningUser(dao)
            mockHostReferrerHash(hash)

            every {
                dao.getThought(1)
            } answers { Thought(1, "userId", "text", ts.toString(), null) }

            every {
                dao.deleteThought(1)
            } just Runs

            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                respond(ofType(RpcData::class.java))
            }
        }
    }

    @Test
    fun testPostThoughtDeleteForbidden() {
        val data = ThoughtDelete(1, 0, "abc")
        val ts = System.currentTimeMillis()
        postThoughtDelete.invokeBlock(locations, data) { handle ->
            every {
                dao.getThought(1)
            } answers { Thought(1, "userId", "text", ts.toString(), null) }

            checkForbiddenIfSesionReturningNothing(handle)
        }
    }

}