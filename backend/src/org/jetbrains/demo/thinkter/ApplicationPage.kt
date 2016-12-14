package org.jetbrains.demo.thinkter

import kotlinx.html.*
import org.jetbrains.ktor.html.*

class ApplicationPage : Template<HTML> {
    val caption = Placeholder<TITLE>()
    val head = Placeholder<HEAD>()

    override fun HTML.apply() {
        head {
            title {
                insert(caption)
            }
            insert(head)
            meta {
                name = "viewport"
                content = "width=device-width, initial-scale=1.0"
            }
        }
        body {
            div {
                id = "content"
            }
            script(src = "frontend/frontend.bundle.js")
        }
    }
}
