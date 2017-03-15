package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import org.jetbrains.common.*
import org.jetbrains.demo.thinkter.model.*
import react.*
import react.dom.*
import kotlin.browser.*

class LoginComponent : ReactDOMComponent<UserProps, LoginFormState>() {
    companion object : ReactComponentSpec<LoginComponent, UserProps, LoginFormState>

    init {
        state = LoginFormState("", "", false, "")
    }

    override fun ReactDOMBuilder.render() {
        div {
            form(classes = "pure-form pure-form-stacked") {
                legEnd { +"Login" }

                fieldSet(classes = "pure-group") {
                    input(type = InputType.text, name = "login") {
                        value = state.login
                        placeholder = "Login"
                        disabled = state.disabled

                        onChangeFunction = {
                            setState {
                                login = it.inputValue
                            }
                        }
                    }
                    input(type = InputType.password, name = "password") {
                        value = state.password
                        placeholder = "Password"
                        disabled = state.disabled

                        onChangeFunction = {
                            setState {
                                password = it.inputValue
                            }
                        }
                    }
                }

                state.errorMessage?.takeIf(String::isNotEmpty)?.let { message ->
                    label {
                        +message
                    }
                }

                button(classes = "pure-button pure-button-primary") {
                    +"Login"
                    disabled = state.disabled

                    onClickFunction = {
                        it.preventDefault()
                        doLogin()
                    }
                }
            }
        }
    }

    private fun doLogin() {
        setState {
            disabled = true
        }

        login(state.login, state.password).then(
                { user -> loggedIn(user) },
                { t -> loginFailed(t) }
        )
    }

    private fun loggedIn(user: User) {
        props.userAssigned(user)
    }

    private fun loginFailed(t: Throwable) {
        if (t is LoginOrRegisterFailedException) {
            setState {
                disabled = false
                errorMessage = t.message
            }
        } else {
            console.error("Login failed", t)
            setState {
                disabled = false
                errorMessage = "Login failed: please reload page and try again"
            }
        }
    }
}

class LoginFormState(var login: String, var password: String, var disabled: Boolean, var errorMessage: String?) : RState