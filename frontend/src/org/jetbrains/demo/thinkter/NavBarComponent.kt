package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import org.jetbrains.demo.thinkter.model.*
import react.*
import react.dom.*

class NavBarComponent : ReactDOMComponent<NavBarComponent.NavBarHandlerProps, BoxedState<User?>>() {

    companion object : ReactComponentSpec<NavBarComponent, NavBarHandlerProps, BoxedState<User?>>

    init {
        state = BoxedState(null)
    }

    override fun ReactDOMBuilder.render() {
        val user = state.state

        ul(classes = "nav-list") {
            if (user != null) {
                navItem("Timeline")
                navItem("New thought")
                navItem("Sign out, ${user.displayName}")
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

    private fun register() {
        props.handler(MainView.Register)
    }

    private fun login() {
        props.handler(MainView.Login)
    }

    private fun UL.navItem(title: String, function: () -> Unit = {}) {
        li(classes = "nav-item") {
            a(classes = "pure-button", href = "javascript:void()") {
                +title
                onClickFunction = { function() }
            }
        }
    }

    class NavBarHandlerProps : RProps() {
        var handler: (MainView) -> Unit = {}
    }
}