package react

import runtime.reflect.createInstance


@DslMarker
annotation class ReactDsl

open class ReactBuilder {
    open class Node<out P : RProps>(
        val type: Any,
        val props: P
    ) {
        var children: ArrayList<Any> = ArrayList()

        open val realType
            get() = type

        fun create() : ReactElement {
            return ReactWrapper.createRaw(realType, props, children)
        }
    }

    val path = mutableListOf<Node<*>>()
    private var lastLeaved: ReactElement? = null

    val children: ArrayList<Any>
        get() = currentNode().children

    fun currentNode(): Node<*> = path.last()
    inline fun <reified T : Node<*>> currentNodeOfType(): T = currentNode() as T

    fun <T: Node<*>> enterNode(node: T) {
        if (path.isEmpty() && lastLeaved != null) {
            console.error("React only allows single element be returned from render() function")
        }
        path.add(node)
    }

    fun exitCurrentNode() : ReactElement {
        val node = path.removeAt(path.lastIndex)
        val element = node.create()
        if (path.isNotEmpty()) {
            children.add(element)
        }
        lastLeaved = element
        return element
    }

    open fun <P: RProps> createReactNode(type: Any, props: P) : Node<RProps> = Node(type, props)

    fun <P: RProps> enterReactNode(type: Any, props: P, handler: ReactBuilder.() -> Unit) : ReactElement {
        enterNode(createReactNode(type, props))
        handler()
        return exitCurrentNode()
    }

    inline fun <reified P : RProps> instantiateProps() : P {
        return P::class.createInstance()
    }

    internal inline operator fun <reified T: ReactComponent<P, S>, reified P : RProps, S: RState> ReactComponentSpec<T, P, S>.invoke(
        noinline handler: P.() -> Unit = {}
    ) : ReactElement {
        val props = instantiateProps<P>()
        return node(props) { props.handler() }
    }

    internal inline operator fun <reified T: ReactComponent<P, S>, reified P : RProps, S: RState> ReactComponentSpec<T, P, S>.invoke(
            props: P,
            noinline handler: P.() -> Unit = {}
    ) : ReactElement {
        return node(props) { props.handler() }
    }

    inline fun <reified T: ReactComponent<P, S>, reified P : RProps, S: RState> ReactComponentSpec<T, P, S>.node(
        props: P,
        noinline handler: ReactBuilder.() -> Unit = {}
    ) = enterReactNode(ReactComponent.wrap(T::class), props, handler)

    internal inline operator fun <reified P : RProps> ReactExternalComponentSpec<P>.invoke(
        noinline handler: P.() -> Unit = {}
    ) : ReactElement {
        val props = instantiateProps<P>()
        return node(props) { props.handler() }
    }

    inline fun <reified P : RProps> ReactExternalComponentSpec<P>.node(
        props: P,
        noinline handler: ReactBuilder.() -> Unit = {}
    ) = enterReactNode(ref, props, handler)

    fun result(): ReactElement? {
        return lastLeaved
    }
}
