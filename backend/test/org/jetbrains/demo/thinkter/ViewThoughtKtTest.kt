package org.jetbrains.demo.thinkter

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit.MockKJUnit4Runner
import io.mockk.mockk
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.ViewThoughtResponse
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.routing.HttpMethodRouteSelector
import org.jetbrains.ktor.routing.Routing
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(MockKJUnit4Runner::class)
class ViewThoughtKtTest {
    val route = mockk<Routing>()
    val dao = mockk<ThinkterStorage>()
    val hash = mockk<(String) -> String>()
    val locations = mockk<Locations>()

    val getViewThought = RouteBlockSlot()
    val postViewThought = RouteBlockSlot()

    @Before
    fun setUp() {
        route.mockDsl(locations) {
            mockObj<ViewThought> {
                mockSelect(HttpMethodRouteSelector(HttpMethod.Get)) {
                    captureBlock(getViewThought)
                }
                mockSelect(HttpMethodRouteSelector(HttpMethod.Post)) {
                    captureBlock(postViewThought)
                }
            }
        }

        route.viewThought(dao, hash)
    }

    @Test
    fun testGetPostThoughtOk() {
        getViewThought.invokeBlock(locations, ViewThought(1)) { handle ->
            mockSessionReturningUser(dao)
            mockHostReferrerHash(hash)
            mockGetThought(dao, 0)

            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                respond(assert<ViewThoughtResponse> {
                    it.thought.userId == "userId" &&
                            it.code!!.contains("cba:tsoh:dIresu")
                })
            }
        }
    }

    @Test
    fun testGetPostThoughtNotLoggedIn() {
        getViewThought.invokeBlock(locations, ViewThought(1)) { handle ->
            mockGetThought(dao, 0)
            mockSessionReturningNothing()

            coEvery<Unit> { respond(any<Any>()) } just Runs

            handle()

            coVerify<Unit> {
                respond(assert<ViewThoughtResponse> {
                    it.thought.id == 1 &&
                            it.code == null
                })
            }
        }
    }

}