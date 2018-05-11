package org.jetbrains.demo.thinkter.enzyme

inline fun configure(block: Configuration.() -> Unit) {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    configure((js("{}") as Configuration).apply(block))
}

inline fun selector(block: dynamic.() -> Unit): dynamic {
    val result = js("{}")
    block(result)
    return result
}