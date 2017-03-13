package react.dom

import org.w3c.dom.*
import react.*

@JsModule("react-dom")
external object ReactDOM {
    fun render(element: ReactElement?, container: Element?)
    fun<P: RProps, S: RState> findDOMNode(component: ReactComponent<P, S>): Element
    fun unmountComponentAtNode(domContainerNode: Element?)
}

fun ReactDOM.render(container: Element?, handler: ReactDOMBuilder.() -> Unit) =
    render(buildElement(handler), container)
