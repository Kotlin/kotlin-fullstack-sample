package org.jetbrains.demo.thinkter

import io.mockk.CapturingSlot
import io.mockk.MockKScope
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.ApplicationFeature
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.pipeline.PipelineContext
import org.jetbrains.ktor.pipeline.PipelineInterceptor
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.routing.RouteSelector
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.routing.application
import org.jetbrains.ktor.sessions.SessionConfig
import org.jetbrains.ktor.util.AttributeKey
import org.jetbrains.ktor.util.Attributes
import kotlin.reflect.KClass

fun Route.mockDsl(locations: Locations, block: RouteDslMock.() -> Unit) = RouteDslMock(this, locations).block()

typealias DslRouteSlot = CapturingSlot<PipelineInterceptor<ApplicationCall>>

class RouteDslMock(val route: Route, val locations: Locations) {
    init {
        every {
            route
                    .application
                    .attributes
                    .childAs(Attributes::class.java)
                    .get(ApplicationFeature.registry)
                    .childAs(Locations::class.java)
                    .get(Locations.key)
        } returns locations
    }

    inline fun <reified T> RouteDslMock.mockObj(noinline block: RouteDslMock.() -> Unit) {
        mockObj(this.route, T::class, block)
    }

    @PublishedApi
    internal fun mockObj(route: Route, dataClass: KClass<*>, block: RouteDslMock.() -> Unit) {
        val nextRoute = mockk<Routing>()
        every { locations.createEntry(route, dataClass) } returns nextRoute
        every { nextRoute.parent } returns route

        RouteDslMock(nextRoute, locations).block()
    }

    fun RouteDslMock.mockSelect(selector: RouteSelector, block: RouteDslMock.() -> Unit) {
        val nextRoute = mockk<Routing>()
        every { this@mockSelect.route.select(selector) } returns nextRoute
        every { nextRoute.parent } returns this@mockSelect.route

        RouteDslMock(nextRoute, locations).block()
    }

    fun RouteDslMock.captureHandle(slot: DslRouteSlot) {
        every { this@captureHandle.route.handle(capture(slot)) } returns null
    }
}

fun DslRouteSlot.issueCall(locations: Locations,
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

inline fun MockKScope.sessionMatcher(): AttributeKey<Session> =
        match({ it!!.name == "Session" })

inline fun MockKScope.sessionConfigMatcher(): AttributeKey<SessionConfig<*>> =
        match({ it!!.name == "SessionConfig" })

