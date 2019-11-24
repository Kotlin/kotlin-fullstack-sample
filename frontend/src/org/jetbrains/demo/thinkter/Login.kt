package org.jetbrains.demo.thinkter

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.html.*
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.jetbrains.common.inputValue
import org.jetbrains.demo.thinkter.model.User
import react.RState
import react.ReactComponentSpec
import react.dom.ReactDOMBuilder
import react.dom.ReactDOMComponent

class LoginComponent : ReactDOMComponent<UserProps, LoginFormState>() {
    companion object : ReactComponentSpec<LoginComponent, UserProps, LoginFormState>

    init {
        state = LoginFormState("", "", false, "")
    }

    override fun ReactDOMBuilder.render() {
        div {
            form(classes = "pure-form pure-form-stacked") {
                legend { +"Login" }

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
        try {
            GlobalScope.async {
                val user = login(state.login, state.password)
                loggedIn(user)
            }
        } catch (err: Exception) {
            loginFailed(err)
        }
    }

    private fun loggedIn(user: User) {
        props.userAssigned(user)
    }

    private fun loginFailed(err: Throwable) {
        if (err is LoginOrRegisterFailedException) {
            setState {
                disabled = false
                errorMessage = err.message
            }
        } else {
            console.error("Login failed", err)
            setState {
                disabled = false
                errorMessage = "Login failed: please reload page and try again"
            }
        }
    }
}

class LoginFormState(var login: String, var password: String, var disabled: Boolean, var errorMessage: String?) : RState
