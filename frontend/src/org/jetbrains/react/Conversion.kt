package react

import runtime.wrappers.*


/**
 * Converts objects to plain js objects AND removing all null fields.
 * As null and undefined have different meaning in js, ALL libraries doesn't expect to have null values
 * and usually just crash.
 * Also we want to support mixing "reacty" and plain kotlin classes in one object with special empty interfaces
 * as we don't have annotations support in Kotlin.JS.
 */

fun toReactyObject(any: Any): dynamic {
    return any.toReactyObject()
}

// Do not inline due Kotlin.JS bug
fun convert(v: Any): dynamic = when (v) {
    is String, is Long, is Int, is Boolean -> v
    is Array<*> -> v.map { convert(it!!) }.toTypedArray()
    is List<*> -> v.map { convert(it!!) }.toTypedArray()
    else -> toReactyObject(v)
}

fun Any.toReactyObject(): dynamic {
    if (this is ReactyCustomStruct) {
        return this.toReacty()
    } else {
        val isKotlinStruct = this is KotlinStruct
        val me = this
        val obj = js("({})")
        for (p in me.getOwnPropertyNames().filterNot { it == "__proto__" || it == "constructor" }) {
            if (js("(me[p] != null)")) {
                val v = me.asDynamic()[p]
                if (isKotlinStruct) {
                    if (v is ReactyStruct) {
                        obj[p] = convert(v)
                    } else {
                        obj[p] = v
                    }
                } else {
                    obj[p] = convert(v)
                }
            }
        }
        return obj
    }
}

/**
 * Reacty Struct with custom reactifier
 */
interface ReactyCustomStruct : ReactyStruct {
    fun toReacty(): dynamic
}

/**
 * Explicitly marking struct as kotlin struct.
 * This means that we are not going to make recurcive copy unless some field have ReactyStruct type
 */
interface KotlinStruct

/**
 * Forcing recursive reactifying
 */
interface ReactyStruct
