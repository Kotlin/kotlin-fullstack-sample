package runtime.wrappers

external fun encodeURIComponent(url: String): String

external fun btoa(url: String): String

external fun require(module: String): dynamic

inline fun <T> jsObject(builder: T.() -> Unit): T {
    val obj: T = js("({})")
    return obj.apply {
        builder()
    }
}

fun <T> Any.toPropsMap(): Map<String, T> {
    val map = mutableMapOf<String, T>()
    val obj = asDynamic()
    for (p in getOwnPropertyNames().filterNot { it in listOf("__proto__", "constructor") }) {
        map[p] = obj[p]
    }
    return map
}

inline fun js(builder: dynamic.() -> Unit): dynamic = jsObject(builder)

fun Any.getOwnPropertyNames(): Array<String> {
    val me = this
    return js("Object.getOwnPropertyNames(me)")
}

/**
 * Copy Kotlin POKO to plain JS, ommiting __proto_ and anything like that.
 */
fun toPlainObject(me: Any): dynamic {
    val obj = js("({})")
    for (p in me.getOwnPropertyNames().filterNot { it == "__proto__" || it == "constructor" }) {
        js("obj[p]=me[p]")
    }
    return obj
}

//fun <T> Map<String, T>.toJsMap(): JsMap<T> {
//    val obj = js("({})")
//    forEach {
//        js("obj[it.key]=it.value")
//    }
//    return obj
//}

// external class JsMap<T>

fun toPlainObjectStripNull(me: Any): dynamic {
    val obj = js("({})")
    for (p in me.getOwnPropertyNames().filterNot { it == "__proto__" || it == "constructor" }) {
        js("if (me[p] != null) { obj[p]=me[p] }")
    }
    return obj
}

fun toPlainDataObjectStripNull(me: Any): dynamic {
    val obj = js("({})")
    for (p in me.getOwnPropertyNames().filterNot { it == "__proto__" || it == "constructor" }) {
        js("if ((typeof me[p] !== \"function\") && (me[p] != null)) { obj[p]=me[p] }")
    }
    return obj
}

fun cloneObject(from: Any, to: Any) {
    for (p in from.getOwnPropertyNames().filterNot { it == "__proto__" || it == "constructor" }) {
        js("if ((typeof from[p] !== \"function\") && (from[p] != null)) { to[p]=from[p] }")
    }
}

fun toPlainDataObjectStripRecursive(me: Any): dynamic {

    fun convert(v: Any): dynamic = when (v) {
        is String, is Long, is Int, is Boolean -> v
        is Array<*> -> v.map { convert(it!!) }.toTypedArray()
        else -> toPlainDataObjectStripRecursive(v)
    }

    val obj = js("({})")
    for (p in me.getOwnPropertyNames().filterNot { it == "__proto__" || it == "constructor" }) {
        if (js("((typeof me[p] !== \"function\") && (me[p] != null))")) {
            val v = me.asDynamic()[p]
            obj[p] = convert(v)
        }
    }
    return obj
}

fun mapsToJs(vararg me: Map<String, String>): dynamic {
    val maps = when {
        me.size == 1 -> me[0]
        else -> HashMap<String, String>().apply {
            for (m1 in me) {
                putAll(m1)
            }
        }
    }
    val obj = js("({})")
    for ((k: String, v: String) in maps.entries) {
        obj[k] = v
    }
    return obj
}

fun properties(subj: dynamic): Array<String> =
    js("var props = []; for (var n in subj) props.push(n); return props")

fun property(subj: dynamic, name: String): Any? = js("subj[name]")

external interface PropertyDescriptor {
}

// https://youtrack.jetbrains.com/issue/KT-16811
external val Object: ObjectConstructor

external interface ObjectConstructor {
    val prototype: Any
    fun getPrototypeOf(o: Any): Any
    fun getOwnPropertyDescriptor(obj: Any, property: String): PropertyDescriptor
    fun getOwnPropertyNames(o: Any): Array<String>
    fun create(): Any
    fun <T> create(o: T): T
    fun <T> create(o: T, properties: PropertyDescriptorMap): T
    fun defineProperty(o: Any, p: String, attributes: PropertyDescriptor): Any
    fun defineProperties(o: Any, properties: PropertyDescriptorMap): Any
    fun <T> seal(o: T): T
    fun <T> freeze(o: T): T
    fun <T> preventExtensions(o: T): T
    fun isSealed(o: Any): Boolean
    fun isFrozen(o: Any): Boolean
    fun isExtensible(o: Any): Boolean
    fun keys(o: Any): Array<String>
    fun <T, R : T> assign(dest: R, src: T): R
}

fun <T> T.clone() = Object.assign(js {}, this) as T
fun <T> T.assign(builder: T.() -> Unit) = clone().apply(builder)

external interface PropertyDescriptorMap {
    fun get(s: String): PropertyDescriptor
}

object KotlinCompiler {
    val metadata = "\$metadata\$"

    object TYPE {
        val CLASS = "class"
        val INTERFACE = "interface"
        val OBJECT = "object"
        val INIT_FUN = "init fun"
    }

}

external interface KMetadata {
    val kind: String
    val simpleName: String
    val interfaces: Array<Any>
}

fun jsstyle(builder: dynamic.() -> Unit): String = js(builder)

@JsName("Set")
external class NativeSet<T> {
    fun add(value: T): NativeSet<T>;
    fun clear()
    fun delete(value: T): Boolean
    fun forEach(callbackfn: (value: T, value2: T, set: Set<T>) -> Unit, thisArg: Any? = definedExternally)
    fun has(value: T): Boolean
    val size: Int
}

external interface TemplateTag<in T, out R> {
    fun call(ctx: Any?, strings: Array<String>, vararg values: T): R
}

operator fun <T, R> TemplateTag<T, R>.invoke(strings: Array<String>, vararg values: T) =
    this.call(null, strings, *values)

operator fun <T, R> TemplateTag<T, R>.invoke(string: String, vararg values: T) = this(arrayOf(string), *values)
operator fun <T, R> TemplateTag<T, R>.invoke(vararg values: T) = this(emptyArray(), *values)
