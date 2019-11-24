package react

import kotlin.properties.*
import kotlin.reflect.*

interface ReactComponentLifecycleListener {
    fun reactComponentDidUpdate(prevProps: RProps, prevState: RState)

    fun reactComponentWillUnmount()

    fun reactComponentDidMount()
}

interface ReactExtensionProvider {
    fun subscribe(listener: ReactComponentLifecycleListener)
    fun unsubscribe(listener: ReactComponentLifecycleListener)
}

abstract class BaseReactExtension(val provider: ReactExtensionProvider) {

    private val listener = object : ReactComponentLifecycleListener {
        override fun reactComponentDidUpdate(prevProps: RProps, prevState: RState) {
            componentDidUpdate(prevProps, prevState)
        }

        override fun reactComponentWillUnmount() {
            provider.unsubscribe(this)
            componentWillUnmount()
        }

        override fun reactComponentDidMount() {
            componentDidMount()
        }
    }

    init {
        provider.subscribe(listener)
    }

    open fun componentDidUpdate(prevProps: RProps, prevState: RState) {}

    open fun componentWillUnmount() {}

    open fun componentDidMount() {}
}

abstract class BaseReactExtensionReadWriteProperty<T>(provider: ReactExtensionProvider) : BaseReactExtension(provider), ReadWriteProperty<Any, T> {

}

abstract class BaseReactExtensionReadOnlyProperty<T>(provider: ReactExtensionProvider) : BaseReactExtension(provider), ReadOnlyProperty<Any, T> {

}

