package org.jetbrains.demo.thinkter

import io.mockk.*
import io.mockk.junit.MockKJUnit4Runner
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.LoginResponse
import org.jetbrains.demo.thinkter.model.User
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.ApplicationFeature
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.pipeline.PipelineInterceptor
import org.jetbrains.ktor.routing.HttpMethodRouteSelector
import org.jetbrains.ktor.routing.RouteSelector
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.sessions.SessionConfig
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

    lateinit var getLogin: DslRouteSlot
    lateinit var postLogin: DslRouteSlot
    lateinit var postLogout: DslRouteSlot

    @Before
    fun setUp() {
        every {
            route
                    .application
                    .attributes
                    .hint(Attributes::class)
                    .get(ApplicationFeature.registry)
                    .hint(Locations::class)
                    .get(Locations.key)
        } returns locations

        getLogin = route.captureDslRoute(
                locations,
                Login::class,
                HttpMethodRouteSelector(HttpMethod.Get))

        postLogin = route.captureDslRoute(
                locations,
                Login::class,
                HttpMethodRouteSelector(HttpMethod.Post))

        postLogout = route.captureDslRoute(
                locations,
                Logout::class,
                HttpMethodRouteSelector(HttpMethod.Post))

        route.login(dao, hash)

    }

    @Test
    fun testGetLoginOk() {
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
            every { attributes.contains(sessionMatcher()) } returns true

            every {
                attributes
                    .hint(Session::class)
                    .get(sessionMatcher())
            } returns Session("userId")

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(LoginResponse(user)) }
        }
    }

    @Test
    fun testGetLoginForbidden() {
        getLogin.issueCall(locations,
                Login("abc",
                        "def",
                        "ghi")) { handle ->
            every { attributes.contains(sessionMatcher()) } returns false

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(HttpStatusCode.Forbidden) }
        }
    }

    @Test
    fun testPostLoginOk() {
        postLogin.issueCall(locations,
                Login("abcdef",
                        "ghiklm")) { handle ->

            every { hash.hint(String::class).invoke("ghiklm") } returns "mlkihg"
            val user = User("abcdef", "abc@def", "Abc Def", "mlkihg")

            every { dao.user("abcdef", "mlkihg") } returns user

            every {
                attributes
                        .hint(SessionConfig::class)
                        .get(sessionConfigMatcher())
                        .sessionType
            } returns Session::class

            every { attributes.put(sessionMatcher(), any()) } just Runs

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(LoginResponse(user)) }

            coVerify { attributes.put(sessionMatcher(), Session("abcdef")) }
        }
    }

    @Test
    fun testPostLoginShortUsername() {
        postLogin.issueCall(locations,
                Login("abc",
                        "defghi")) { handle ->

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(LoginResponse(error = "Invalid username or password")) }
        }
    }

    @Test
    fun testPostLoginShortPassword() {
        postLogin.issueCall(locations,
                Login("abcdef",
                        "ghi")) { handle ->

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(LoginResponse(error = "Invalid username or password")) }
        }
    }

    @Test
    fun testPostLoginWrongUsername() {
        postLogin.issueCall(locations,
                Login("#!$%#$$@#",
                        "defghi")) { handle ->

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(LoginResponse(error = "Invalid username or password")) }
        }
    }

    @Test
    fun testPostLogoutOk() {
        postLogout.issueCall(locations,
                Logout()) { handle ->

            every { hash.hint(String::class).invoke("ghiklm") } returns "mlkihg"
            val user = User("abcdef", "abc@def", "Abc Def", "mlkihg")

            every { dao.user("abcdef", "mlkihg") } returns user

            every {
                attributes
                        .hint(SessionConfig::class)
                        .getOrNull(sessionConfigMatcher())
            } returns null

            every { attributes.remove(sessionMatcher()) } just Runs

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(HttpStatusCode.OK) }

            verify { attributes.remove(sessionMatcher()) }
        }
    }
}

private fun <T : Any> Routing.captureDslRoute(locations: Locations,
                                              dataClass: KClass<T>,
                                              selector: RouteSelector): DslRouteSlot {

    every {
        locations.createEntry(this@captureDslRoute, dataClass)
                .select(selector)
                .parent
    } returns this

    val lambda = slot<PipelineInterceptor<ApplicationCall>>()
    every {
        locations.createEntry(this@captureDslRoute, dataClass)
                .select(selector)
                .handle(capture(lambda))
    } just Runs
    return lambda
}

