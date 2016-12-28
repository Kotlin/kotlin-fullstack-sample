package org.jetbrains.demo.thinkter

import kotlinx.html.*
import org.jetbrains.components.*
import org.jetbrains.interop.*
import org.jetbrains.react.*
import kotlin.browser.*

fun main(args: Array<String>) {
    require("./resources/main.css");

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
            h1 { +"Thoughts" }
            +"… list of thought …"
        }
    }

}

class ToolbarProps
class Toolbar(props: ToolbarProps) : ReactComponent<ToolbarProps, dynamic>(props) {
    override fun render() = element {
        nav("toolbar horizontal fixed-top") {
            div("content") {
                div("content-start") {
                    div("item") {
                        LinkProps("/").by(::Link) { +"Home" }
                    }
                }
                div("content-end") {
                    div("item") {
                        LinkProps("/profile").by(::Link) { +"Profile" }
                    }
                }
                div("item") {
                    LinkProps("/thoughts").by(::Link) { +"Thoughts" }
                }
            }
        }
    }

}

val themeSettings = getMuiTheme(props {
    palette = props {
        accent1Color = deepOrange500
    }
})

class Application(props: dynamic) : ReactComponent<dynamic, dynamic>(props) {
    override fun render() = element {
        div {
            onComponent(MuiThemeProvider, props {
                this.muiTheme = themeSettings
            }) {
                div() {
                    appbar("Thinkter") {
                        iconButton {
                            fontIcon("material-icons") { +"home" }
                        }
                    }

                    appendElement(props.children)
                }
            }
        }
    }
}


