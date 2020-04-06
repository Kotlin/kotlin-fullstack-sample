package org.jetbrains.demo.thinkter

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.p
import org.jetbrains.demo.thinkter.model.Thought
import react.RProps
import react.RState
import react.ReactComponentSpec
import react.dom.ReactDOMBuilder
import react.dom.ReactDOMComponent

class HomeView : ReactDOMComponent<HomeView.Props, HomeView.State>() {
    companion object : ReactComponentSpec<HomeView, Props, State>

    init {
        state = State(emptyList(), emptyList(), true, Polling.NewMessages.None)
    }

    override fun componentDidMount() {
        super.componentDidMount()

        props.polling.listeners.add(pollerHandler)
        loadHome()
    }

    override fun componentWillUnmount() {
        super.componentWillUnmount()
        props.polling.listeners.remove(pollerHandler)
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
        GlobalScope.launch {
            val r = index()
            props.polling.start()
            setState {
                loading = false
                top = r.top
                latest = r.latest
            }
        }
    }

    private val pollerHandler = { m: Polling.NewMessages ->
        val oldMessages = state.newMessages
        setState {
            newMessages = m
        }
        if (oldMessages != m && m == Polling.NewMessages.None) {
            loadHome()
        }
    }

    class State(var top: List<Thought>, var latest: List<Thought>, var loading: Boolean, var newMessages: Polling.NewMessages) : RState
    class Props(var polling: Polling, var showThought: (Thought) -> Unit = {}) : RProps()
}