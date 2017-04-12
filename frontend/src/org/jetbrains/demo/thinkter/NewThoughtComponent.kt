package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import org.jetbrains.common.*
import org.jetbrains.demo.thinkter.model.*
import react.*
import react.dom.*

class NewThoughtComponent : ReactDOMComponent<NewThoughtComponent.Props, NewThoughtComponent.State>() {
    companion object : ReactComponentSpec<NewThoughtComponent, Props, State>

    init {
        state = State()
    }

    override fun ReactDOMBuilder.render() {
        form(classes = "pure-form pure-form-stacked") {
            legEnd {
                if (props.replyTo == null) {
                    +"New thought"
                } else {
                    +"Reply"
                }
            }

            props.replyTo?.let { replyTo ->
                div {
                    + "reply to ${replyTo.userId}"
                }
            }

            textArea(classes = "pure-input-1-2") {
                placeholder = "Your thought..."

                onChangeFunction = {
                    setState {
                        text = it.inputValue
                    }
                }
            }

            state.errorMessage?.let { message ->
                p { +message }
            }

            button(classes = "pure-button pure-button-primary") {
                + "Post"

                onClickFunction = {
                    it.preventDefault()
                    doPostThought()
                }
            }
        }
    }

    private fun doPostThought() {
        postThoughtPrepare().then({ t ->
            postThought(props.replyTo?.id, state.text, t).then({ thought ->
                onSubmitted(thought)
            }, { onFailed(it) }).catch { onFailed(it) }
        }, { onFailed(it) }).catch { onFailed(it) }
    }

    private fun onSubmitted(thought: Thought) {
        setState {
            errorMessage = null
        }

        props.showThought(thought)
    }

    private fun onFailed(t: Throwable) {
        setState {
            errorMessage = t.message
        }
    }

    class State(var text: String = "", var errorMessage: String? = null) : RState
    class Props(var replyTo: Thought? = null, var replyToUser: User? = null, var showThought: (Thought) -> Unit = {}) : RProps()
}