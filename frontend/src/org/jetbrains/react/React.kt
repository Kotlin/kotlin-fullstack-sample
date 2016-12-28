package org.jetbrains.react

import org.jetbrains.interop.*
import org.w3c.dom.*

@JsModule("react")
external object React {
    fun createElement(type: (Any?) -> ReactElement, prop: Any?, vararg children: dynamic): ReactElement
    fun createElement(type: dynamic, prop: Any?, vararg children: dynamic): ReactElement
}

inline fun <reified TComponent : ReactComponent<TProps, *>, TProps>
        ReactElementBuilder.insert(props: TProps, noinline body: ReactElementBuilder.() -> Unit = {}) {
    onComponent(reactClass<TComponent>(), props, body)
}


inline fun <reified TComponent : ReactComponent<*, *>> reactClass(): dynamic {
    val componentType: dynamic = TComponent::class.js
    val displayName = nameOf<TComponent>()
    if (displayName != null)
        componentType.displayName = displayName
    return componentType
}

fun Element?.react(body: ReactElementBuilder.() -> Unit) {
    if (this == null)
        throw IllegalArgumentException("DOM Element is null")
    ReactDOM.render(ReactElementBuilder().apply(body).finalize(), this)
}

inline fun props(builder: dynamic.() -> Unit): dynamic = jsObject(builder)
