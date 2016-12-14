package org.jetbrains.interop

@JsModule("jquery")
external object jquery {
    fun ajax(configure: dynamic): Unit = noImpl
}

fun ajax(configure: dynamic.() -> Unit): Unit {
    val configureObject = js("({})")
    configureObject.configure()
    jquery.ajax(configureObject)
}
