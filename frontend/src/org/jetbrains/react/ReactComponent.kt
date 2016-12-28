@file:JsModule("react")

package org.jetbrains.react

import org.w3c.dom.*

@JsName("ComponentLifecycle")
external interface ReactComponentLifecycle<in TProps, in TState> {
    fun componentWillMount() {
    }

    fun componentDidMount() {
    }

    fun componentWillUnmount() {
    }

    fun componentWillReceiveProps(nextProps: TProps, nextContext: Any) {
    }

    fun shouldComponentUpdate(nextProps: TProps, nextState: TState, nextContext: Any): Boolean = true
    fun componentWillUpdate(nextProps: TProps, nextState: TState, nextContext: Any) {
    }

    fun componentDidUpdate(prevProps: TProps, prevState: TState, prevContext: Any) {
    }
}


@JsName("Component")
external abstract class ReactComponent<TProps, TState>(val props: TProps) : ReactComponentLifecycle<TProps, TState> {
    val refs: dynamic get() = noImpl

    var state: TState = noImpl;

    abstract fun render(): ReactElement

    /**
     * If this component has been mounted into the DOM, this returns the corresponding native browser DOM element.
     * This method is useful for reading values out of the DOM, such as form field values and performing DOM measurements.
     */
    fun getDOMNode(): Element = noImpl

    /**
     * When you're integrating with an external JavaScript application you may want to signal a change to a React component rendered with renderComponent().
     * Simply call setProps() to change its properties and trigger a re-render.
     *
     * @param nextProps the object that will be merged with the component's props
     * @param callback an optional callback function that is executed once setProps is completed.
     */
    fun setProps(nextProps: TProps, callback: (() -> Unit)?): Unit = noImpl

    /**
     * Like setProps() but deletes any pre-existing props instead of merging the two objects.
     *
     * @param nextProps the object that will replace the component's props
     * @param callback an optional callback function that is executed once replaceProps is completed.
     */
    fun replaceProps(nextProps: TProps, callback: () -> Unit): Unit = noImpl

    /**
     * Transfer properties from this component to a target component that have not already been set on the target component.
     * After the props are updated, targetComponent is returned as a convenience.
     *
     * @param target the component that will receive the props
     */
    fun <C : ReactComponent<TProps, Any>> transferPropsTo(target: C): C = noImpl

    /**
     * Merges nextState with the current state.
     * This is the primary method you use to trigger UI updates from event handlers and server request callbacks.
     * In addition, you can supply an optional callback function that is executed once setState is completed.
     *
     * @param nextState the object that will be merged with the component's state
     * @param callback an optional callback function that is executed once setState is completed.
     */
    fun setState(nextState: TState, callback: () -> Unit ): Unit = noImpl

    /**
     * Merges nextState with the current state.
     * This is the primary method you use to trigger UI updates from event handlers and server request callbacks.
     *
     * @param nextState the object that will be merged with the component's state
     */
    fun setState(nextState: TState): Unit = noImpl

    /**
     * If your render() method reads from something other than this.props or this.state,
     * you'll need to tell React when it needs to re-run render() by calling forceUpdate().
     * You'll also need to call forceUpdate() if you mutate this.state directly.
     * Calling forceUpdate() will cause render() to be called on the component and its children,
     * but React will still only update the DOM if the markup changes.
     * Normally you should try to avoid all uses of forceUpdate() and only read from this.props and this.state in render().
     * This makes your application much simpler and more efficient.
     *
     * @param callback an optional callback that is executed once forceUpdate is completed.
     */
    fun forceUpdate(callback: () -> Unit): Unit = noImpl
}

open external class NativeReactComponent() : ReactComponent<dynamic, dynamic>(null) {
    override fun render(): ReactElement = noImpl
}

fun <TProps, TState> ReactComponent<TProps, TState>.setState(block: dynamic.() -> Unit): Unit {
    val obj = js("({})")
    obj.block()
    setState(obj as TState)
}
