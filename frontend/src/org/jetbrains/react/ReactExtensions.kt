package react

import kotlin.properties.*
import kotlin.reflect.*

interface ReactComponentLifecycleListener {
    fun reactComponentWillUpdate()

    fun reactComponentWillUnmount()

    fun reactComponentWillMount()

    fun reactComponentDidMount()
}

interface ReactExtensionProvider {
    fun subscribe(listener: ReactComponentLifecycleListener)
    fun unsubscribe(listener: ReactComponentLifecycleListener)
}

abstract class BaseReactExtension(val provider: ReactExtensionProvider) {

    private val listener = object : ReactComponentLifecycleListener {
        override fun reactComponentWillUpdate() {
            componentWillUpdate()
        }

        override fun reactComponentWillUnmount() {
            provider.unsubscribe(this)
            componentWillUnmount()
        }

        override fun reactComponentWillMount() {
            componentWillMount()
        }

        override fun reactComponentDidMount() {
            componentDidMount()
        }
    }

    init {
        provider.subscribe(listener)
    }

    open fun componentWillUpdate() {}

    open fun componentWillUnmount() {}

    open fun componentWillMount() {}

    open fun componentDidMount() {}
}

abstract class BaseReactExtensionReadWriteProperty<T>(provider: ReactExtensionProvider) : BaseReactExtension(provider), ReadWriteProperty<Any, T> {

}

abstract class BaseReactExtensionReadOnlyProperty<T>(provider: ReactExtensionProvider) : BaseReactExtension(provider), ReadOnlyProperty<Any, T> {

}

