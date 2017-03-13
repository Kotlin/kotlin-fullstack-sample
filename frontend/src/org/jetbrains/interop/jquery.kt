package org.jetbrains.interop

@JsModule("jquery")
external object jquery {
    fun ajax(configure: dynamic): Unit
}

fun ajax(configure: dynamic.() -> Unit): Unit {
    val configureObject = js("({})")
    configure(configureObject)
    jquery.ajax(configureObject)
}
