@file:JsModule("react-router")

package org.jetbrains.react

class IndexProps(val component: ReactComponent<*, *>)
class RouteProps(val path: String, val component: ReactComponent<*, *>)
class RouterProps(val history: dynamic)
class LinkProps(val to: String)

external class Router(props: RouterProps) : ReactComponent<RouterProps, dynamic>(props) {
    override fun render(): ReactElement = noImpl
}

external class Route(props: RouteProps) : ReactComponent<RouteProps, dynamic>(props) {
    override fun render(): ReactElement = noImpl
}

external class IndexRoute(props: IndexProps) : ReactComponent<IndexProps, dynamic>(props) {
    override fun render(): ReactElement = noImpl
}

external class Link(props: LinkProps) : ReactComponent<LinkProps, dynamic>(props) {
    override fun render(): ReactElement = noImpl
}


external val browserHistory: dynamic

fun ReactElementBuilder.routing(body: ReactElementBuilder.() -> Unit) {
    insert<Router, dynamic>(RouterProps(browserHistory), body)
}

inline fun <reified TComponent : ReactComponent<*, *>>
        ReactElementBuilder.route(route: String, noinline body: ReactElementBuilder.() -> Unit = {}) {
    val componentType = reactClass<TComponent>()
    appendRoute(RouteProps(route, componentType), body)
}

inline fun <reified TComponent : ReactComponent<*, *>>
        ReactElementBuilder.index(noinline body: ReactElementBuilder.() -> Unit = {}) {
    val componentType = reactClass<TComponent>()
    appendIndex(IndexProps(componentType), body)
}

fun ReactElementBuilder.appendRoute(props: dynamic, body: ReactElementBuilder.() -> Unit) {
    insert<Route, dynamic>(props, body)
}

fun ReactElementBuilder.appendIndex(props: dynamic, body: ReactElementBuilder.() -> Unit) {
    insert<IndexRoute, dynamic>(props, body)
}