package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import org.jetbrains.demo.thinkter.model.*
import react.*
import react.dom.*

class NavBarComponent : ReactDOMComponent<NavBarComponent.NavBarHandlerProps, ReactComponentNoState>() {

    companion object : ReactComponentSpec<NavBarComponent, NavBarHandlerProps, ReactComponentNoState>

    init {
        state = ReactComponentNoState()
    }

    override fun ReactDOMBuilder.render() {
        val user = props.user

        ul(classes = "nav-list") {
            if (user != null) {
                navItem("Timeline") {
                    timeline()
                }
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
        logoutUser().then({
            props.logoutHandler()
        })
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

    class NavBarHandlerProps : RProps() {
        var user: User? = null
        var logoutHandler: () -> Unit = {}
        var handler: (MainView) -> Unit = {}
    }
}