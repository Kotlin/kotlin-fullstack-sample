package org.jetbrains.demo.thinkter

import io.mockk.*
import io.mockk.junit.MockKJUnit4Runner
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.LoginResponse
import org.jetbrains.demo.thinkter.model.User
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.ApplicationFeature
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.pipeline.PipelineContext
import org.jetbrains.ktor.pipeline.PipelineInterceptor
import org.jetbrains.ktor.routing.HttpMethodRouteSelector
import org.jetbrains.ktor.routing.RouteSelector
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.util.Attributes
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.reflect.KClass

@RunWith(MockKJUnit4Runner::class)
class LoginKtTest {
    val route = mockk<Routing>()
    val dao = mockk<ThinkterStorage>()
    val hash = mockk<(String) -> String>()
    val locations = mockk<Locations>()

    lateinit var getLogin: AppCallSlot
    lateinit var postLogin: AppCallSlot
    lateinit var postLogout: AppCallSlot

    @Before
    fun setUp() {
        every {
            route
                    .application
                    .attributes
                    .childAs(Attributes::class.java)
                    .get(ApplicationFeature.registry)
                    .childAs(Locations::class.java)
                    .get(Locations.key)
        } returns locations

        getLogin = route.mockCall(
                locations,
                Login::class,
                HttpMethodRouteSelector(HttpMethod.Get))

        postLogin = route.mockCall(
                locations,
                Login::class,
                HttpMethodRouteSelector(HttpMethod.Post))

        postLogout = route.mockCall(
                locations,
                Logout::class,
                HttpMethodRouteSelector(HttpMethod.Post))

        route.login(dao, hash)

    }

    @Test
    fun testLoginForbidden() {
        getLogin.issueCall(locations,
                Login("abc",
                        "def",
                        "ghi")) { handle ->
            every {
                attributes.contains(match { it!!.name == "Session" })
            } returns false

            coEvery {
                respond(any())
            } returns null

            handle()

            coVerify {
                respond(HttpStatusCode.Forbidden)
            }
        }
    }

    @Test
    fun testLoginOk() {
        val user = User("userId",
                "email",
                "display",
                "pwd")
        every {
            dao.user("userId", any())
        } returns user

        getLogin.issueCall(locations,
                Login("abc",
                        "def",
                        "ghi")) { handle ->
            every {
                attributes.contains(match { it!!.name == "Session" })
            } returns true

            every {
                attributes.get(match { it!!.name == "Session" })
            } returns Session("userId")

            coEvery {
                respond(any())
            } returns null

            handle()

            coVerify {
                respond(LoginResponse(user))
            }
        }
    }

    private fun <T : Any> Routing.mockCall(locations: Locations,
                                           dataClass: KClass<T>,
                                           selector: RouteSelector): CapturingSlot<PipelineInterceptor<ApplicationCall>> {

        every {
            this@mockCall
                    .application
                    .attributes
                    .childAs(Attributes::class.java)
                    .get(ApplicationFeature.registry)
                    .childAs(Locations::class.java)
                    .get(Locations.key)
        } returns locations


        every {
            locations.createEntry(this@mockCall, dataClass)
                    .select(selector)
                    .parent
        } returns this

        val lambda = slot<PipelineInterceptor<ApplicationCall>>()
        every {
            locations.createEntry(this@mockCall, dataClass)
                    .select(selector)
                    .handle(capture(lambda))
        } returns null
        return lambda
    }
}

typealias AppCallSlot = CapturingSlot<PipelineInterceptor<ApplicationCall>>

private fun AppCallSlot.issueCall(locations: Locations,
                                  data: Any,
                                  block: ApplicationCall.(() -> Unit) -> Unit) {

    runBlocking {
        val ctx = mockk<PipelineContext<ApplicationCall>>()
        val call = mockk<ApplicationCall>()
        every {
            locations.childAs(data.javaClass)
                    .resolve<Any>(data.javaClass.kotlin, call)
        } returns data

        every {
            ctx.childAs(ApplicationCall::class.java)
                    .subject
        } returns call

        call.block({
            runBlocking {
                captured!!.invoke(ctx, call)
            }
        })
    }

}
