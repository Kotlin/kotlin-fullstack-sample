package react

import runtime.wrappers.*

interface ReactElement

object React {

    private val raw: ReactSpec = require("react")

    fun normalize(child: Any?) : List<Any> = when(child) {
        null -> listOf()
        is Iterable<*> -> child.filterNotNull()
        is Array<*> -> child.filterNotNull()
        else -> listOf(child)
    }

    fun createRaw(type: Any, props: dynamic, child: Any? = null): ReactElement =
       raw.createElement(type, toPlainObjectStripNull(props), *normalize(child).toTypedArray())
}
