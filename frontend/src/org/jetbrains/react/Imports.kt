package react

external interface ReactUpdater {
    fun enqueueSetState(dest: Any, state: Any?)
    fun enqueueReplaceState(dest: Any, state: Any?)
    fun enqueueCallback(dest: Any, callback: Any, method: String)
}

@JsModule("react")
@JsNonModule
external object React {
    fun createElement(type: Any, props: dynamic, vararg child: Any): ReactElement
}
