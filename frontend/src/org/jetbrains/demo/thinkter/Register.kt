package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import org.jetbrains.common.*
import org.jetbrains.demo.thinkter.model.*
import react.*
import react.dom.*
import kotlin.browser.*


class RegisterComponent : ReactDOMComponent<UserProps, RegisterFormState>() {
    companion object : ReactComponentSpec<RegisterComponent, UserProps, RegisterFormState>

    init {
        state = RegisterFormState("", "", "", "", null, false)
    }

    override fun ReactDOMBuilder.render() {
        div {
            form(classes = "pure-form pure-form-stacked") {
                legEnd { +"Register" }

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
                        + message
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
        with(state) {
            register(login, password, displayName, email)
                    .then({ user -> registered(user) })
                    .catch { e -> registrationFailed(e) }
        }
    }

    private fun registered(user: User) {
        props.userAssigned(user)
    }

    private fun registrationFailed(e: Throwable) {
        if (e is LoginOrRegisterFailedException) {
            setState {
                errorMessage = e.message
                disabled = false
            }
        } else {
            console.log("Registration failed", e)
            setState {
                errorMessage = "Registration failed"
            }
        }
    }
}

class RegisterFormState(var login: String, var displayName: String, var password: String, var email: String, var errorMessage: String?, var disabled: Boolean) : RState