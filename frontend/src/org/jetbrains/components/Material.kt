@file:JsModule("material-ui")

package org.jetbrains.components

import org.jetbrains.react.*

external class AppBar : NativeReactComponent()
fun ReactElementBuilder.appbar(
        title: String,
        block: ReactElementBuilder.() -> Unit
) = onComponent(reactClass<AppBar>(), props {
    this.title = title
}, block)

external val ToolBar: ReactElement
external val ToolBarGroup: ReactElement
external val ToolbarSeparator: ReactElement
external val RaisedButton: ReactElement
external val IconMenu: ReactElement
external val MenuItem: ReactElement
external val DropDownMenu: ReactElement

external class FontIcon: NativeReactComponent()
fun ReactElementBuilder.fontIcon(
        classes: String,
        block: ReactElementBuilder.() -> Unit
) = onComponent(reactClass<IconButton>(), props {
    this.className = classes
}, block)

external class IconButton: NativeReactComponent()
fun ReactElementBuilder.iconButton(
        block: ReactElementBuilder.() -> Unit
) = onComponent(reactClass<IconButton>(), props {
}, block)

fun ReactElementBuilder.fontIconButton(name: String) {
    iconButton {
        fontIcon("material-icons") { +name }
    }
}

