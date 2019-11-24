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


class RegisterComponent : ReactDOMComponent<UserProps, RegisterFormState>() {
    companion object : ReactComponentSpec<RegisterComponent, UserProps, RegisterFormState>

    init {
        state = RegisterFormState("", "", "", "", null, false)
    }

    override fun ReactDOMBuilder.render() {
        div {
            form(classes = "pure-form pure-form-stacked") {
                legend { +"Register" }

                fieldSet(classes = "pure-group") {
                    input(type = InputType.text, name = "login") {
                        value = state.login
                        placeholder = "Login"
                        disabled = state.disabled

                        onChangeFunction = { e ->
                            setState {
                                login = e.inputValue
                            }
                        }
                    }
                    input(type = InputType.text, name = "email") {
                        value = state.email
                        placeholder = "Email"
                        disabled = state.disabled

                        onChangeFunction = { e ->
                            setState {
                                email = e.inputValue
                            }
                        }
                    }
                    input(type = InputType.password, name = "password") {
                        value = state.password
                        placeholder = "Password"
                        disabled = state.disabled

                        onChangeFunction = { e ->
                            setState {
                                password = e.inputValue
                            }
                        }
                    }
                }
                fieldSet(classes = "pure-group") {
                    input(type = InputType.text, name = "displayName") {
                        value = state.displayName
                        placeholder = "Display name"
                        disabled = state.disabled

                        onChangeFunction = { e ->
                            setState {
                                displayName = e.inputValue
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
                    +"Register"
                    disabled = state.disabled

                    onClickFunction = {
                        doRegister()
                        it.preventDefault()
                    }
                }
            }
        }
    }

    private fun doRegister() {
        setState {
            disabled = true
        }
        try {
            GlobalScope.async {
                with(state) {
                    val user = register(login, password, displayName, email)
                    registered(user)
                }
            }
        } catch (err: Exception) {
            registrationFailed(err)
        }
    }

    private fun registered(user: User) {
        props.userAssigned(user)
    }

    private fun registrationFailed(err: Throwable) {
        if (err is LoginOrRegisterFailedException) {
            setState {
                errorMessage = err.message
                disabled = false
            }
        } else {
            console.log("Registration failed", err)
            setState {
                errorMessage = "Registration failed"
            }
        }
    }
}

class RegisterFormState(var login: String, var displayName: String, var password: String, var email: String, var errorMessage: String?, var disabled: Boolean) : RState
