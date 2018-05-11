@file:JsModule("enzyme")

package org.jetbrains.demo.thinkter.enzyme

external interface AdapterBase

external interface Configuration {
    var adapter: AdapterBase
}

external fun configure(conf: Configuration)

external fun shallow(o: dynamic, options: dynamic = definedExternally): ShallowWrapper

external interface ShallowWrapper {
    fun find(selector: dynamic): ShallowWrapper
    fun exists(): Boolean
}