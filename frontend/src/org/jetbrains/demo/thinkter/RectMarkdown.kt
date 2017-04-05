package org.jetbrains.demo.thinkter

import react.*

private val ReactMarkdown: dynamic = runtime.wrappers.require("react-markdown")

class RectMarkdownProps(var source: String = "") : RProps()

object ReactMarkdownComponent : ReactExternalComponentSpec<RectMarkdownProps>(ReactMarkdown)
