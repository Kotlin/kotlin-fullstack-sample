package org.jetbrains.demo.thinkter

import kotlinx.html.*
import kotlinx.html.js.*
import org.jetbrains.demo.thinkter.model.*
import react.*
import react.dom.*

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
                +text
            }

            if (props.currentUser != null) {
                div(classes = "pure-u-3 pure-u-md-2-3") {
                    +""
                }
                div(classes = "pure-u-3 pure-u-md-1-3") {
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

    class Props(var thought: Thought, var currentUser: User? = null, var reply: (Thought) -> Unit = {}) : RProps()
}