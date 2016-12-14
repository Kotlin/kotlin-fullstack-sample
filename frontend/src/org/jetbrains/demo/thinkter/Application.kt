package org.jetbrains.demo.thinkter

import kotlinx.html.*
import org.jetbrains.react.*
import kotlin.browser.*

fun main(args: Array<String>) {
    document.getElementById("content").react {
        div {
            routing {
                route<Application>("/") {

                }
            }
        }
    }
}

class Application : ReactComponent<dynamic, dynamic>(null) {
    override fun render() = element {
        div {
            +"Hello, world!"
        }
    }
}


