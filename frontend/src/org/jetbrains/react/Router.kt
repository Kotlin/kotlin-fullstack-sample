@file:JsModule("react-router")
package org.jetbrains.react

import org.jetbrains.interop.*

external class Router : ReactComponent<dynamic, dynamic>(null) {
    override fun render(): ReactElement = noImpl
}

external class Route : ReactComponent<dynamic, dynamic>(null) {
    override fun render(): ReactElement = noImpl
}

external class IndexRoute : ReactComponent<dynamic, dynamic>(null) {
    override fun render(): ReactElement = noImpl
}

external class Link : ReactComponent<dynamic, dynamic>(null) {
    override fun render(): ReactElement = noImpl
}

external val browserHistory: dynamic

fun ReactElementBuilder.routing(body: ReactElementBuilder.() -> Unit) {
    val props = js { history = browserHistory }
    insert<Router, dynamic>(props, body)
}

inline fun <reified TComponent : ReactComponent<*, *>>
        ReactElementBuilder.route(route: String, noinline body: ReactElementBuilder.() -> Unit = {}) {
    val componentType = reactClass<TComponent>()
    val props = js {
        path = route
        component = componentType
    }
    appendRoute(props, body)
}

inline fun <reified TComponent : ReactComponent<*, *>>
        ReactElementBuilder.index(noinline body: ReactElementBuilder.() -> Unit = {}) {
    val componentType = reactClass<TComponent>()
    val props = js {
        component = componentType
    }
    appendIndex(props, body)
}

fun ReactElementBuilder.appendRoute(props: dynamic, body: ReactElementBuilder.() -> Unit) {
    insert<Route, dynamic>(props, body)
}

fun ReactElementBuilder.appendIndex(props: dynamic, body: ReactElementBuilder.() -> Unit) {
    insert<IndexRoute, dynamic>(props, body)
}