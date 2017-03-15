package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import react.*
import react.dom.*


class RegisterComponent : ReactDOMPropslessComponent<BoxedState<RegisterFormState>>() {
    companion object : ReactComponentSpec<RegisterComponent, ReactComponentNoProps, BoxedState<RegisterFormState>>

    init {
        state = BoxedState(RegisterFormState("", "", "", ""))
    }

    override fun ReactDOMBuilder.render() {
        div {
            form(classes = "pure-form pure-form-stacked") {
                legEnd { +"Register" }

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
                    input(type = InputType.text, name = "email") {
                        value = state.state.password
                        placeholder = "Email"
                        onChangeFunction = {
                            setState {
                                state = state.copy(email = this@input.value)
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
                fieldSet(classes = "pure-group") {
                    input(type = InputType.text, name = "displayName") {
                        value = state.state.login
                        placeholder = "Display name"
                        onChangeFunction = {
                            setState {
                                state = state.copy(displayName = value)
                            }
                        }
                    }
                }

                button(classes = "pure-button pure-button-primary") {
                    +"Register"
                    onClickFunction = {
                        doRegister()
                        it.preventDefault()
                    }
                }
            }
        }
    }

    fun doRegister() {
    }
}

data class RegisterFormState(val login: String, val displayName: String, val password: String, val email: String)