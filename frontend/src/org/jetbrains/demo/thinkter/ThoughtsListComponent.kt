package org.jetbrains.demo.thinkter

import kotlinx.html.*
import org.jetbrains.demo.thinkter.model.*
import react.*
import react.dom.*

class ThoughtsListComponent : ReactDOMComponent<ReactComponentNoProps, BoxedState<List<Thought>>>() {
    companion object : ReactComponentSpec<ThoughtsListComponent, ReactComponentNoProps, BoxedState<List<Thought>>>

    init {
        state = BoxedState(emptyList())
    }

    override fun ReactDOMBuilder.render() {
        div {
            ul {
                if (state.state.isEmpty()) {
                    li { +"There are no thoughts yet" }
                } else {
                    for (t in state.state) {
                        thoughtLi(t)
                    }
                }
            }
        }
    }

    private fun UL.thoughtLi(t: Thought) {
        li {
            section(classes = "post") {
                header(classes = "post-header") {
                    p(classes = "post-meta") {
                        a(href = "javascript:void") {
                            +t.date
                            +" by "
                            +t.userId
                        }
                    }
                }
                div(classes = "post-description") {
                    +t.text
                }
            }
        }
    }
}