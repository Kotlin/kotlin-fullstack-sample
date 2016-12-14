package org.jetbrains.demo.thinkter

import kotlinx.html.*
import org.jetbrains.interop.*
import org.jetbrains.react.*
import kotlin.browser.*

fun main(args: Array<String>) {
    require("./resources/blog.css");

    document.getElementById("content").react {
        div {
            routing {
                route<Application>("/") {
                    index<Home>()
                }
            }
        }
    }
}

class Home(props: dynamic) : ReactComponent<dynamic, dynamic>(props) {
    override fun render() = element {
        div {
            +"HOME"
        }
    }

}

class ToolbarProps
class Toolbar(props: ToolbarProps) : ReactComponent<ToolbarProps, dynamic>(props) {
    override fun render() = element {
        div {
            +"Toolbar"
        }
    }

}

class Application(props: dynamic) : ReactComponent<dynamic, dynamic>(props) {
    override fun render() = element {
        div {
            ToolbarProps().by(::Toolbar)
            h1 { +"Thinkster" }
            appendElement(props.children)
        }
    }
}


