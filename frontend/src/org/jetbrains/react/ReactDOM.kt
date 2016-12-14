package org.jetbrains.react

import org.w3c.dom.*

@JsModule("react-dom")
external object ReactDOM {
    val version: String

    fun render(element: ReactElement, container: Element?)
    fun findDOMNode(component: ReactComponent<*, *>): Element
    fun findDOMNode(component: ReactElement): Element
    fun unmountComponentAtNode(container: Element?)
}


