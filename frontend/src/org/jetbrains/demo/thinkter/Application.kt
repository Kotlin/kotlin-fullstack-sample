package org.jetbrains.demo.thinkter

import kotlinx.html.*
import react.*
import react.dom.*
import kotlin.browser.*

fun main(args: Array<String>) {
    ReactDOM.render(document.getElementById("content")) {
        div {
            Application {}
        }
    }
}

class Home : ReactDOMComponent<ReactComponentNoProps, ReactComponentNoState>() {
    companion object : ReactComponentSpec<Home, ReactComponentNoProps, ReactComponentNoState>

    override fun ReactDOMBuilder.render() {
        div {
            h1 { +"Thoughts" }
            +"… list of thought …"
        }
    }

}

class Application : ReactDOMComponent<ReactComponentNoProps, ReactComponentNoState>() {
    companion object : ReactComponentSpec<Application, ReactComponentNoProps, ReactComponentNoState>

    override fun ReactDOMBuilder.render() {
        div {
            Home {}
        }
    }
}


