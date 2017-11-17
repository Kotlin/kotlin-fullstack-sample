package org.jetbrains.demo.thinkter

import io.mockk.*
import io.mockk.junit.MockKJUnit4Runner
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.LoginResponse
import org.jetbrains.demo.thinkter.model.User
import org.jetbrains.ktor.application.ApplicationFeature
import org.jetbrains.ktor.http.HttpHeaders
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.routing.HttpMethodRouteSelector
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.routing.application
import org.jetbrains.ktor.util.Attributes
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class RegisterKtTest {
    val route = mockk<Routing>()
    val dao = mockk<ThinkterStorage>()
    val hash = mockk<(String) -> String>()
    val locations = mockk<Locations>()

    val getRegister = RouteBlockSlot()
    val postRegister = RouteBlockSlot()

    @Before
    fun setUp() {
        route.mockDsl(locations) {
            mockObj<Register> {
                mockSelect(HttpMethodRouteSelector(HttpMethod.Post)) {
                    captureBlock(postRegister)
                }
                mockSelect(HttpMethodRouteSelector(HttpMethod.Get)) {
                    captureBlock(getRegister)
                }
            }
        }

        route.register(dao, hash)
    }

    @Test
    fun testPostRegisterOk() {
        val data = Register(userId = "abcdef", password = "abcdefghi")
        postRegister.invokeBlock(locations, data) { handle ->
            mockSessionReturningNothing()
            mockHostReferrerHash(hash)
            mockPutSession()

            every { dao.user("abcdef") } returns null

            every { dao.createUser(any()) } just Runs

            coEvery<Unit> { respond(any<Any>()) } just Runs

            handle()

            coVerify {
                respond(assert<LoginResponse> {
                    it.user?.userId == "abcdef"
                })
            }

        }
    }

    @Test
    fun testPostRegisterLoggedIn() {
        val data = Register()
        postRegister.invokeBlock(locations, data) { handle ->
            mockSessionReturningUser(dao)

            every { request.headers[HttpHeaders.Host] } returns "host"

            every {
                application
                        .attributes
                        .get(ApplicationFeature.registry)
                        .get(Locations.key)
                        .href(any())
            } returns "/redirect"

            every {
                response.headers.append(any(), any())
            } just Runs

            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                response.headers.append(HttpHeaders.Location, "http://host/redirect")
                respond(HttpStatusCode.Found)
            }
        }
    }


    @Test
    fun testPostRegisterPasswordSize() {
        checkRegisterError(
                Register(userId = "abcdefghi", password = "abcd"),
                "Password should be at least 6 characters long")
    }

    @Test
    fun testPostRegisterLoginSize() {
        checkRegisterError(
                Register(userId = "abc", password = "abcdefghi"),
                "Login should be at least 4 characters long")
    }

    @Test
    fun testPostRegisterLoginConsistsOfDigitsLetters() {
        checkRegisterError(
                Register(userId = "#@!$!#$", password = "abcdefghi"),
                "Login should be consists of digits, letters, dots or underscores")
    }

    private fun checkRegisterError(data: Register, msg: String) {
        postRegister.invokeBlock(locations, data) { handle ->
            mockSessionReturningNothing()

            coEvery { respond(any()) } just Runs

            handle()

            coVerify {
                respond(LoginResponse(error = msg))
            }
        }
    }

    @Test
    fun testPostRegisterSameUserRegistered() {
        val data = Register(userId = "abcdef", password = "abcdefghi")
        postRegister.invokeBlock(locations, data) { handle ->
            mockSessionReturningNothing()
            mockHostReferrerHash(hash)

            val user = User("abcdef", "abc@def", "Abc Def", "")
            every { dao.user("abcdef") } returnsMany listOf(null, user)

            every { dao.createUser(any()) } throws RuntimeException("failed to create user")

            coEvery<Unit> { respond(any<Any>()) } just Runs

            handle()

            coVerify {
                respond(LoginResponse(error = "User with the following login is already registered"))
            }

        }
    }

    @Test
    fun testPostRegisterSameEmailUserRegistered() {
        val data = Register(userId = "abcdef", password = "abcdefghi", email = "user@email")
        postRegister.invokeBlock(locations, data) { handle ->
            mockSessionReturningNothing()
            mockHostReferrerHash(hash)

            every { dao.user(any()) } returns null

            every { dao.createUser(any()) } throws RuntimeException("failed to create user")

            every { dao.userByEmail("user@email") } returns mockk()

            coEvery<Unit> { respond(any<Any>()) } just Runs

            handle()

            coVerify {
                respond(LoginResponse(error = "User with the following email user@email is already registered"))
            }
        }
    }

    @Test
    fun testPostRegisterFailed() {
        val data = Register(userId = "abcdef", password = "abcdefghi", email = "user@email")
        postRegister.invokeBlock(locations, data) { handle ->
            mockSessionReturningNothing()
            mockHostReferrerHash(hash)

            every { dao.user(any()) } returns null

            every { dao.createUser(any()) } throws RuntimeException("failed to create user")

            every { dao.userByEmail("user@email") } returns null

            every { route.application.environment.log.error(any<String>()) } just Runs

            coEvery<Unit> { respond(any<Any>()) } just Runs

            handle()

            coVerify {
                respond(LoginResponse(error = "Failed to register"))
            }
        }
    }

    @Test
    fun testPostRegisterUserExists() {
        val data = Register(userId = "abcdef", password = "abcdefghi")
        postRegister.invokeBlock(locations, data) { handle ->
            mockSessionReturningNothing()
            mockUser(dao)

            coEvery<Unit> { respond(any<Any>()) } just Runs

            handle()

            coVerify {
                respond(LoginResponse(error = "User with the following login is already registered"))
            }

        }
    }

    @Test
    fun testGetRegisterNotAllowed() {
        getRegister.invokeBlock(locations, Register()) { handle ->
            coEvery { respond(any()) } just Runs

            handle()

            coVerify { respond(HttpStatusCode.MethodNotAllowed) }
        }
    }
}