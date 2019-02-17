package org.jetbrains.demo.thinkter

import io.mockk.*
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.LoginResponse
import org.jetbrains.demo.thinkter.model.User
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.routing.HttpMethodRouteSelector
import org.jetbrains.ktor.routing.Routing
import org.junit.Before
import org.junit.Test

class LoginKtTest {
    val route = mockk<Routing>()
    val dao = mockk<ThinkterStorage>()
    val hash = mockk<(String) -> String>()
    val locations = mockk<Locations>()

    val getLogin = RouteBlockSlot()
    val postLogin = RouteBlockSlot()
    val postLogout = RouteBlockSlot()

    @Before
    fun setUp() {
        route.mockDsl(locations) {
            mockObj<Login> {
                mockSelect(HttpMethodRouteSelector(HttpMethod.Get)) {
                    captureBlock(getLogin)
                }
                mockSelect(HttpMethodRouteSelector(HttpMethod.Post)) {
                    captureBlock(postLogin)
                }
            }
            mockObj<Logout> {
                mockSelect(HttpMethodRouteSelector(HttpMethod.Post)) {
                    captureBlock(postLogout)
                }
            }
        }

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

        getLogin.invokeBlock(locations,
                Login("abc",
                        "def",
                        "ghi")) { handle ->
            every { attributes.contains(sessionMatcher()) } returns true

            every {
                attributes
                    .get(sessionMatcher())
            } returns Session("userId")

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(LoginResponse(user)) }
        }
    }

    @Test
    fun testGetLoginNotLoggedIn() {
        getLogin.invokeBlock(locations,
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
        postLogin.invokeBlock(locations,
                Login("abcdef",
                        "ghiklm")) { handle ->

            every { hash.invoke("ghiklm") } returns "mlkihg"

            val user = mockUser(dao, "mlkihg")

            mockPutSession()

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(LoginResponse(user)) }

            coVerify { attributes.put(sessionMatcher(), Session("abcdef")) }
        }
    }

    @Test
    fun testPostLoginShortUsername() {
        postLogin.invokeBlock(locations,
                Login("abc",
                        "defghi")) { handle ->

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(LoginResponse(error = "Invalid username or password")) }
        }
    }

    @Test
    fun testPostLoginShortPassword() {
        postLogin.invokeBlock(locations,
                Login("abcdef",
                        "ghi")) { handle ->

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(LoginResponse(error = "Invalid username or password")) }
        }
    }

    @Test
    fun testPostLoginWrongUsername() {
        postLogin.invokeBlock(locations,
                Login("#!$%#$$@#",
                        "defghi")) { handle ->

            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(LoginResponse(error = "Invalid username or password")) }
        }
    }

    @Test
    fun testPostLogoutOk() {
        postLogout.invokeBlock(locations,
                Logout()) { handle ->

            every { hash.invoke("ghiklm") } returns "mlkihg"

            every {
                dao.user("abcdef", "mlkihg")
            } returns User("abcdef",
                    "abc@def",
                    "Abc Def",
                    "mlkihg")

            every {
                attributes
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
