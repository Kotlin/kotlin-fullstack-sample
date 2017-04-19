package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import org.jetbrains.demo.thinkter.model.*
import react.*
import react.dom.*
import runtime.wrappers.*
import kotlin.browser.*
import kotlinx.coroutines.experimental.launch

class ViewThoughtComponent : ReactDOMComponent<ViewThoughtComponent.Props, ReactComponentNoState>() {

    companion object : ReactComponentSpec<ViewThoughtComponent, Props, ReactComponentNoState>

    init {
        state = ReactComponentNoState()
    }

    override fun ReactDOMBuilder.render() {
        val userId = props.thought.userId
        val text = props.thought.text
        val date = props.thought.date

        div(classes = "pure-g") {
            div(classes = "pure-u-1 pure-u-md-1-3") {
                +userId
                props.thought.replyTo?.let { id ->
                    +" replies to $id"
                }
            }
            div(classes = "pure-u-1 pure-u-md-2-3") {
                +date
            }
            div(classes = "pure-u-2 pure-u-md-1-1") {
                ReactMarkdownComponent {
                    source = text
                }
            }

            if (props.currentUser != null) {
                div(classes = "pure-u-3 pure-u-md-2-3") {
                    +""
                }
                div(classes = "pure-u-3 pure-u-md-1-3") {
                    a(href = "javascript:void(0)") {
                        +"Delete"

                        onClickFunction = {
                            delete()
                        }
                    }

                    span {
                        style = jsstyle { margin = "0 5px 0 5px" }
                        +" "
                    }

                    a(href = "javascript:void(0)") {
                        +"Reply"

                        onClickFunction = {
                            props.reply(props.thought)
                        }
                    }
                }
            }
        }
    }

    private fun delete() {
        if (window.confirm("Do you want to delete the thought?")) {
            launch {
                val token = postThoughtPrepare()
                deleteThought(props.thought.id, token.date, token.code)
                props.leave
            }
        }
    }

    class Props(var thought: Thought, var currentUser: User? = null, var reply: (Thought) -> Unit = {}, var leave: () -> Unit = {}) : RProps()
}