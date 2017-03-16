package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import org.jetbrains.demo.thinkter.model.*
import react.*
import react.dom.*
import kotlin.browser.*

fun main(args: Array<String>) {
    runtime.wrappers.require("pure-blog.css")

    ReactDOM.render(document.getElementById("content")) {
        div {
            Application {}
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
                            user = state.currentUser
                            handler = { navBarSelected(it) }
                            logoutHandler = { onLoggedOut() }
                        }
                    }
                }
            }

            div("content pure-u-1 pure-u-md-3-4") {
                when (state.selected) {
                    MainView.Home -> HomeView {
                        showThought = { t -> onShowThought(t) }
                    }
                    MainView.Login -> LoginComponent {
                        userAssigned = { onUserAssigned(it) }
                    }
                    MainView.Register -> RegisterComponent {
                        userAssigned = { onUserAssigned(it) }
                    }
                    MainView.PostThought -> NewThoughComponent {
                        showThought = { t -> onShowThought(t) }
                    }
                    MainView.User -> {}
                    MainView.Though -> ViewThoughtComponent {
                        thought = state.currentThought ?: Thought(0, "?", "?", "?", null)
                    }
                }
            }

            div("footer") {
                +"Thinkter kotlin frontend + react + ktor example"
            }
        }
    }

    private fun onLoggedOut() {
        val oldSelected = state.selected

        setState {
            currentUser = null
            selected = when (oldSelected) {
                MainView.Home, MainView.Though, MainView.Login, MainView.Register -> oldSelected
                else -> MainView.Home
            }
        }
    }

    private fun onShowThought(t: Thought) {
        setState {
            currentThought = t
            selected = MainView.Though
        }
    }

    private fun navBarSelected(newSelected: MainView) {
        setState {
            selected = newSelected
        }
    }

    private fun onUserAssigned(user: User) {
        setState {
            currentUser = user
            selected = MainView.Home
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
    PostThought,
    Though,
    Home
}

class ApplicationPageState(var selected: MainView, var currentUser: User? = null, var currentThought: Thought? = null) : RState
class UserProps : RProps() {
    var userAssigned: (User) -> Unit = {}
}
