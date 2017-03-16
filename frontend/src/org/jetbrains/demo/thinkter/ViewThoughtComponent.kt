package org.jetbrains.demo.thinkter

import kotlinx.html.*
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
            }
            div(classes = "pure-u-1 pure-u-md-2-3") {
                +date
            }
            div(classes = "pure-u-2 pure-u-md-1-1") {
                +text
            }
        }
    }

    class Props(var thought: Thought) : RProps()
}