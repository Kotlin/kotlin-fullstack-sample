package org.jetbrains.demo.thinkter

import kotlinx.html.*
import org.jetbrains.demo.thinkter.model.*
import react.*
import react.dom.*

class HomeView : ReactDOMComponent<HomeView.Props, HomeView.State>() {
    companion object : ReactComponentSpec<HomeView, Props, State>

    init {
        state = State(emptyList(), emptyList(), true)
        loadHome()
    }

    override fun ReactDOMBuilder.render() {
        div {
            h2 { +"Thoughts" }

            if (state.loading) {
                p { +"Loading..." }
            } else {
                h3 { +"Top" }
                ThoughtsListComponent {
                    thoughts = state.top
                    show = props.showThought
                }

                h3 { +"Recent" }
                ThoughtsListComponent {
                    thoughts = state.latest
                    show = props.showThought
                }
            }
        }
    }

    private fun loadHome() {
        index().then({ r ->
            setState {
                loading = false
                top = r.top
                latest = r.latest
            }
        })
    }

    class State(var top: List<Thought>, var latest: List<Thought>, var loading: Boolean) : RState
    class Props(var showThought: (Thought) -> Unit = {}) : RProps()
}