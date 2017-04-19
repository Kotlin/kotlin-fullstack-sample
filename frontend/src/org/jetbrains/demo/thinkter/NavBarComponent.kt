package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import org.jetbrains.demo.thinkter.model.*
import react.*
import react.dom.*
import kotlinx.coroutines.experimental.launch

class NavBarComponent : ReactDOMComponent<NavBarComponent.NavBarHandlerProps, NavBarComponent.NavBarState>() {

    companion object : ReactComponentSpec<NavBarComponent, NavBarHandlerProps, NavBarState>

    init {
        state = NavBarState()
    }

    override fun componentDidMount() {
        props.poller?.let { p ->
            p.listeners.add(pollerHandler)
            p.start()
        }

        super.componentDidMount()
    }

    override fun componentWillUnmount() {
        super.componentWillUnmount()
        props.poller?.listeners?.remove(pollerHandler)
    }

    override fun ReactDOMBuilder.render() {
        val user = props.user
        val newMessages = state.newMessages

        ul(classes = "nav-list") {
            val timelineText = "Timeline" + when (newMessages) {
                Polling.NewMessages.None -> ""
                is Polling.NewMessages.Few -> "(${newMessages.n})"
                is Polling.NewMessages.MoreThan -> "(${newMessages.n}+"
            }

            navItem(timelineText) {
                props.poller?.start()
                timeline()
                setState {
                    this.newMessages = Polling.NewMessages.None
                }
            }

            if (user != null) {
                navItem("New thought") {
                    postNew()
                }
                navItem("Sign out, ${user.displayName.takeIf(String::isNotBlank) ?: user.userId}") {
                    logout()
                }
            } else {
                navItem("Sign up") {
                    register()
                }
                navItem("Sign in") {
                    login()
                }
            }
        }
    }

    private val pollerHandler = { count: Polling.NewMessages ->
        setState {
            newMessages = count
        }
    }

    private fun timeline() {
        props.handler(MainView.Home)
    }

    private fun register() {
        props.handler(MainView.Register)
    }

    private fun login() {
        props.handler(MainView.Login)
    }

    private fun logout() {
        launch {
            logoutUser()
            props.logoutHandler()
        }
    }

    private fun postNew() {
        props.handler(MainView.PostThought)
    }

    private fun UL.navItem(title: String, function: () -> Unit = {}) {
        li(classes = "nav-item") {
            a(classes = "pure-button", href = "javascript:void(0)") {
                +title
                onClickFunction = { function() }
            }
        }
    }

    class NavBarState(var newMessages: Polling.NewMessages = Polling.NewMessages.None) : RState

    class NavBarHandlerProps : RProps() {
        var user: User? = null
        var logoutHandler: () -> Unit = {}
        var handler: (MainView) -> Unit = { }
        var poller: Polling? = null
    }
}