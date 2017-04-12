package runtime.reflect

import kotlin.reflect.KClass

fun <T : Any> KClass<T>.createInstance(): T {
    @Suppress("UNUSED_VARIABLE")
    val ctor = this.js

    return js("new ctor()")
}

