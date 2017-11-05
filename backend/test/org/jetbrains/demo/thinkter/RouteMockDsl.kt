package org.jetbrains.demo.thinkter

import io.mockk.*
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
import org.jetbrains.ktor.util.Attributes
import kotlin.reflect.KClass

fun Route.mockDsl(locations: Locations, block: RouteDslMock.() -> Unit) = RouteDslMock(this, locations).block()

typealias RouteBlockSlot = CapturingSlot<PipelineInterceptor<ApplicationCall>>

class RouteDslMock(val route: Route, val locations: Locations) {
    init {
        every {
            route
                    .application
                    .attributes
                    .hint(Attributes::class)
                    .get(ApplicationFeature.registry)
                    .hint(Locations::class)
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
        every { route.select(selector) } returns nextRoute
        every { nextRoute.parent } returns route

        RouteDslMock(nextRoute, locations).block()
    }

    fun RouteDslMock.captureBlock(slot: RouteBlockSlot) {
        every { route.handle(capture(slot)) } just Runs
    }
}

typealias CallInvoker = () -> Unit

fun RouteBlockSlot.invokeBlock(locations: Locations,
                               data: Any,
                               block: ApplicationCall.(CallInvoker) -> Unit) {

    runBlocking {
        val ctx = mockk<PipelineContext<ApplicationCall>>()

        val call = mockk<ApplicationCall>()

        every {
            val dataCls = data.javaClass.kotlin
            locations.hint(dataCls)
                    .resolve<Any>(dataCls, call)
        } returns data

        every {
            ctx.hint(ApplicationCall::class)
                    .subject
        } returns call

        call.block {
            runBlocking {
                captured.invoke(ctx, call)
            }
        }
    }

}
