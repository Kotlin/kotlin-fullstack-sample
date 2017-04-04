package react

import runtime.wrappers.toPlainObjectStripNull

interface ReactElement

internal object ReactWrapper {
    fun normalize(child: Any?) : List<Any> = when(child) {
        null -> listOf()
        is Iterable<*> -> child.filterNotNull()
        is Array<*> -> child.filterNotNull()
        else -> listOf(child)
    }

    fun createRaw(type: Any, props: dynamic, child: Any? = null): ReactElement =
            React.createElement(type, toPlainObjectStripNull(props), *normalize(child).toTypedArray())
}
