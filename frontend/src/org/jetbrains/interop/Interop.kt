package org.jetbrains.interop

inline fun <reified T> nameOf(): String? = T::class.simpleName

fun Any.getOwnPropertyNames(): Array<String> {
    val me = this
    return js("Object.getOwnPropertyNames(me)")
}

/**
 * Copy Kotlin POKO to plain JS, ommiting __proto_ and anything like that.
 */
fun Any.toPlainObject(): dynamic {
    @Suppress("UNUSED_VARIABLE")
    val me = this

    val obj = js("({})")
    for (p in getOwnPropertyNames()) {
        js("obj[p]=me[p]")
    }
    return obj
}

inline fun <T> jsObject(builder: T.() -> Unit): T = (js("({})") as T).apply(builder)
inline fun js(builder: dynamic.() -> Unit): dynamic = jsObject(builder)

external fun require(module: String)