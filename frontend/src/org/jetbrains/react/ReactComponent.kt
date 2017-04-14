package react

import runtime.reflect.createInstance
import kotlin.reflect.KClass

abstract class RProps {
    var key: String? = null
    var children: Any? = null
}

external interface RState

class BoxedState<T>(var state: T) : RState

interface ReactComponentSpec<T : ReactComponent<P, S>, P : RProps, S : RState>


private var initWrapper: ReactComponentWrapper<*, *, *>? = null

abstract class ReactComponent<P : RProps, S : RState> : ReactExtensionProvider {

    internal val wrapper = initWrapper as ReactComponentWrapper<*, *, S>
    internal lateinit var stateField: S
    internal var isSealed = false
    internal var hasState = false
    val props: P
        get() = wrapper.props.asDynamic()

    var state: S
        get() = stateField
        set(value) {
            if (!isSealed) {
                stateField = value
                hasState = true
            } else {
                throw RuntimeException("You can't set initial state not in constructor")
            }
        }

    fun setState(builder: S.() -> Unit) {
        if (!isSealed) {
            state.builder()
        } else {
            wrapper.setState(builder)
        }
    }

    fun replaceState(state: S) {
        if (!isSealed) {
            this.state = state
        } else {
            wrapper.replaceState(state)
        }
    }

    internal fun seal() {
        isSealed = true
    }

    internal fun setStateFromWrapper(state: S) {
        stateField = state
        hasState = true
    }

    companion object {

        private val wrappers = HashMap<Any, Any>()

        inline fun <reified K, P : RProps, S : RState> wrap(): (P, Any, ReactUpdater) -> ReactComponentWrapper<K, P, S>  where K : ReactComponent<P, S> {
            return wrap(K::class)
        }

        fun <K, P : RProps, S : RState> wrap(clazz: KClass<K>): (P, Any, ReactUpdater) -> ReactComponentWrapper<K, P, S>  where K : ReactComponent<P, S> {
            if (wrappers[clazz] == null) {
                wrappers[clazz] = { p: P, context: Any, updater: ReactUpdater -> ReactComponentWrapper(p, updater, clazz) }
                wrappers[clazz].asDynamic().displayName = clazz.js.name
            }
            return wrappers[clazz] as (P, Any, ReactUpdater) -> ReactComponentWrapper<K, P, S>
        }
    }

    abstract fun render(): ReactElement?

    open fun componentWillMount() {

    }

    open fun componentDidMount() {

    }

    open fun componentWillUnmount() {

    }

    open fun componentDidUpdate(prevProps: P, prevState: S) {

    }

    open fun shouldComponentUpdate(nextProps: P, nextState: S): Boolean {
        return true
    }

    open fun componentWillUpdate() {

    }

    open fun componentWillReceiveProps(nextProps: P) {

    }

    override fun subscribe(listener: ReactComponentLifecycleListener) {
        wrapper.subscribers.add(listener)
    }

    override fun unsubscribe(listener: ReactComponentLifecycleListener) {
        wrapper.subscribers.remove(listener)
    }
}

//
// Wrapper Class
// Passed directly to React and proxifies all method calls to a real one
// Created for not mixing react and kotlin (overridable) functions and for having ability
// to alter our component's behaviour with powerful kotlin black magic
//

class ReactComponentWrapper<K, P : RProps, S : RState>(var props: P, val updater: ReactUpdater, val klazz: KClass<K>) where K : ReactComponent<P, S> {

    private val delegate: K
    private var stateField: S
    var state: S
        get() = stateField
        set(value) {
            stateField = value
            delegate.setStateFromWrapper(value)
        }
    var subscribers = ArrayList<ReactComponentLifecycleListener>()

    init {
        val oldGlobal = initWrapper
        initWrapper = this
        delegate = klazz.createInstance()
        delegate.seal()
        initWrapper = oldGlobal

        if (!delegate.hasState) {
            throw RuntimeException("You haven't set initial state in your constructor of ${klazz.simpleName}!")
        }
        this.stateField = delegate.state
    }

    fun setState(stateBuilder: S.() -> Unit) {
        val partialState: S = js("({})")
        partialState.stateBuilder()

        updater.enqueueSetState(this, partialState)
    }

    fun replaceState(state: S) {
        updater.enqueueReplaceState(this, state)
    }

    @JsName("render")
    fun render(): ReactElement? {
        return delegate.render()
    }

    @JsName("shouldComponentUpdate")
    fun shouldComponentUpdate(nextProps: P, nextState: S): Boolean {
        return delegate.shouldComponentUpdate(nextProps, nextState)
    }

    @JsName("componentWillReceiveProps")
    fun componentWillReceiveProps(nextProps: P) {
        delegate.componentWillReceiveProps(nextProps)
    }

    @JsName("componentWillUpdate")
    fun componentWillUpdate() {
        subscribers.forEach {
            it.reactComponentWillUpdate()
        }
        delegate.componentWillUpdate()
    }

    @JsName("componentDidUpdate")
    fun componentDidUpdate(prevProps: P, prevState: S) {
        delegate.componentDidUpdate(prevProps, prevState)
    }

    @JsName("componentWillUnmount")
    fun componentWillUnmount() {
        subscribers.forEach {
            it.reactComponentWillUnmount()
        }
        delegate.componentWillUnmount()
    }

    @JsName("componentWillMount")
    fun componentWillMount() {
        subscribers.forEach {
            it.reactComponentWillMount()
        }
        delegate.componentWillMount()
    }

    @JsName("componentDidMount")
    fun componentDidMount() {
        subscribers.forEach {
            it.reactComponentDidMount()
        }
        delegate.componentDidMount()
    }
}
