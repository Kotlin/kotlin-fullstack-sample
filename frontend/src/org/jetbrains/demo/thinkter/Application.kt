package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import react.*
import react.dom.*
import runtime.wrappers.*
import kotlin.browser.*

fun main(args: Array<String>) {
    runtime.wrappers.require("pure-blog.css")

    ReactDOM.render(document.getElementById("content")) {
        div {
            Application {}
        }
    }
}

class Home : ReactDOMComponent<ReactComponentNoProps, ReactComponentNoState>() {
    companion object : ReactComponentSpec<Home, ReactComponentNoProps, ReactComponentNoState>

    init {
        state = ReactComponentNoState()
    }

    override fun ReactDOMBuilder.render() {
        div {
            h2 { +"Thoughts" }
            ThoughtsListComponent {}
        }
    }

}

class Application : ReactDOMComponent<ReactComponentNoProps, ApplicationPageState>() {
    companion object : ReactComponentSpec<Application, ReactComponentNoProps, ApplicationPageState>

    init {
        state = ApplicationPageState(MainView.Home)
    }

    override fun ReactDOMBuilder.render() {
        div("pure-g") {
            div("sidebar pure-u-1 pure-u-md-1-4") {
                div("header") {
                    div("brand-title") {
                        +"Thinkter"

                        onClickFunction = { mainViewSelected() }
                    }
                    nav("nav") {
                        NavBarComponent {
                            handler = { navBarSelected(it) }
                        }
                    }
                }
            }

            div("content pure-u-1 pure-u-md-3-4") {
                when (state.selected) {
                    MainView.Home -> Home {}
                    MainView.Login -> LoginComponent {}
                    MainView.Register -> RegisterComponent {}
                    MainView.User -> {}
                    MainView.Though -> {}
                }
            }

            div("footer") {
                +"Thinkter kotlin frontend + react + ktor example"
            }
        }
    }

    private fun navBarSelected(newSelected: MainView) {
        setState {
            selected = newSelected
        }
    }

    private fun mainViewSelected() {
        setState {
            selected = MainView.Home
        }
    }
}

enum class MainView {
    Register,
    Login,
    User,
    Though,
    Home
}

class ApplicationPageState(var selected: MainView) : RState

