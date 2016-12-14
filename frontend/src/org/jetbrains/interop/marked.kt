package org.jetbrains.interop

@JsModule("marked")
external fun marked(markdown: String, sanitize: Boolean): String
