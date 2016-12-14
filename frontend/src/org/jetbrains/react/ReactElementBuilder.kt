package org.jetbrains.react

import kotlinx.html.*
import org.jetbrains.interop.*
import org.w3c.dom.events.*

external interface ReactElement

fun element(body: ReactElementBuilder.() -> Unit): ReactElement = ReactElementBuilder().apply(body).finalize()

class ReactElementBuilder : TagConsumer<ReactElement> {
    private data class Node(val component: dynamic, var children: dynamic, val props: dynamic)

    private val path = mutableListOf<Node>()
    private var lastLeaved: ReactElement? = null

    private fun currentNode(): Node = path.last()

    private fun appendChild(child: dynamic) {
        if (path.isEmpty()) throw IllegalStateException("No current DOM node")
        val node = currentNode()
        val children = node.children
        if (children is Array<*>) {
            children.push(child)
        } else if (children === undefined) {
            node.children = child
        } else {
            node.children = arrayOf(children, child)
        }
    }

    override fun finalize(): ReactElement {
        return lastLeaved ?: throw IllegalStateException("No tags were emitted")
    }

    override fun onTagAttributeChange(tag: Tag, attribute: String, value: String?) {
        val node = currentNode()
        node.setAttribute(attribute, value)
    }

    private fun Node.setAttribute(attribute: String, value: String?) {
        if (value == null) {
            js("delete node.props[value]")
        } else {
            val reactName = when (attribute) {
                "class" -> "className"
                else -> attribute.toReactEventName()
            }
            props[reactName] = value
        }
    }

    override fun onTagContent(content: CharSequence) = appendChild(content)
    override fun onTagContentEntity(entity: Entities) = appendChild(entity.text)
    override fun onTagContentUnsafe(block: Unsafe.() -> Unit) {
        val sb = StringBuilder()
        object : Unsafe {
            override fun String.unaryPlus() {
                sb.append(this)
            }
        }.block()
        val node = currentNode()

        node.props["dangerouslySetInnerHTML"] = object {
            @Suppress("unused")
            val __html = sb.toString()
        }
    }

    override fun onTagStart(tag: Tag) {
        val node = Node(tag.tagName, undefined, js("({})"))
        tag.attributesEntries.forEach { node.setAttribute(it.key, it.value) }

        if (path.isEmpty() && lastLeaved != null) {
            console.error("React only allows single element be returned from render() function")
        }
        path.add(node)
    }

    override fun onTagEnd(tag: Tag) {
        if (path.isEmpty() || currentNode().component.toLowerCase() != tag.tagName.toLowerCase())
            throw IllegalStateException("We haven't entered tag ${tag.tagName} but trying to leave")
        val node = path.removeAt(path.lastIndex)
        val element = React.createElement(node.component, node.props, *node.children)
        if (path.isNotEmpty())
            appendChild(element)
        lastLeaved = element
    }

    override fun onTagEvent(tag: Tag, event: String, value: (Event) -> Unit) {
        val node = currentNode()
        node.props[event.toReactEventName()] = value
    }

    fun onComponent(component: dynamic, props: dynamic, body: ReactElementBuilder.() -> Unit = {}) {
        val plained = (props as Any).toPlainObject()
        val node = Node(component, undefined, plained)
        path.add(node)
        body()
        path.removeAt(path.lastIndex)

        val element = React.createElement(node.component, node.props, node.children)
        if (path.isNotEmpty()) {
            appendChild(element)
        }
        lastLeaved = element
    }

    inline fun <reified TComponent : ReactComponent<TProps, *>, TProps>
            TProps.by(component: (TProps) -> TComponent, noinline body: ReactElementBuilder.() -> Unit = {}) {
        onComponent(reactClass<TComponent>(), this, body)
    }

    infix inline fun <reified TComponent : ReactComponent<TProps, *>, TProps>
            TProps.by(component: (TProps) -> TComponent) {
        onComponent(reactClass<TComponent>(), this)
    }

}

var TEXTAREA.value: String
    get() = attributes["value"] ?: ""
    set(value) {
        attributes["value"] = value
    }