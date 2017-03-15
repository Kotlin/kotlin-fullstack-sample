package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import react.*
import react.dom.*

class LoginComponent : ReactDOMPropslessComponent<BoxedState<LoginFormState>>() {
    companion object : ReactComponentSpec<LoginComponent, ReactComponentNoProps, BoxedState<LoginFormState>>

    init {
        state = BoxedState(LoginFormState("", ""))
    }

    override fun ReactDOMBuilder.render() {
        div {
            form(classes = "pure-form pure-form-stacked") {
                legEnd { +"Login" }

                fieldSet(classes = "pure-group") {
                    input(type = InputType.text, name = "login") {
                        value = state.state.login
                        placeholder = "Login"
                        onChangeFunction = {
                            setState {
                                state = state.copy(login = value)
                            }
                        }
                    }
                    input(type = InputType.password, name = "password") {
                        value = state.state.password
                        placeholder = "Password"
                        onChangeFunction = {
                            setState {
                                state = state.copy(password = this@input.value)
                            }
                        }
                    }
                }

                button(classes = "pure-button pure-button-primary") {
                    +"Login"
                    onClickFunction = {
                        doLogin()
                        it.preventDefault()
                    }
                }
            }
        }
    }

    fun doLogin() {
    }
}

data class LoginFormState(val login: String, val password: String)