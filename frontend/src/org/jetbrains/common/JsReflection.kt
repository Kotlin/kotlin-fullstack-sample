package runtime.reflect

import runtime.wrappers.*
import kotlin.reflect.*

fun <T : Any> KClass<T>.createInstance(): T {
    @Suppress("UNUSED_VARIABLE")
    val ctor = this.js

    return js("new ctor()")
}

fun <T : Any> KClass<T>.createInstance(arg: Any): T {
    @Suppress("UNUSED_VARIABLE")
    val ctor = this.js

    @Suppress("UNUSED_VARIABLE")
    val arg1 = arg
    return js("new ctor(arg1)")
}

fun KClass<*>.jsMetadata(): JsClassMetadata {
    return JsReflection.getClassMetadata(this)
}

val KClass<*>.jsFullClassName
    get() = if (jsMetadata().packageName != "") jsMetadata().packageName + "." + simpleName!! else simpleName!!

val KClass<*>.jsPackageName
    get() = jsMetadata().packageName

fun KClass<*>.jsFindSubclasses(): Array<KClass<*>> {
    return JsReflection.findSubclasses(this)
}

fun <T> KClass<*>.jsFindCompanion(): T? {
    val comp = js.asDynamic().Companion
    if (comp is T) {
        return comp
    } else {
        return null
    }
}

class JsClassMetadata(val clazz: KClass<*>, val module: Any, val packageName: String, val kMetadata: KMetadata)

object JsReflection {

    private val moduleClasses = HashMap<Any, Array<JsClassMetadata>>()
    private val classMetadata = HashMap<KClass<*>, JsClassMetadata>()

    fun enrichReflection(module: Any) {
        val classes = findAllClasses("", module)
        moduleClasses.put(module, classes)
        for (i in classes) {
            classMetadata[i.clazz] = i
        }
    }

    internal fun getClassMetadata(clazz: KClass<*>): JsClassMetadata {
        return classMetadata[clazz] ?: throw RuntimeException("Class ${clazz.simpleName} is not enriched. Did you call enrichReflection on required module?")
    }

    internal fun findSubclasses(base: KClass<*>): Array<KClass<*>> {
        return moduleClasses.values.flatMap {
            it.filter {
                it.kMetadata.interfaces.any { it == base.js }
            }.map { it.clazz }
        }.toTypedArray()
    }

    private fun findAllClasses(pkg: String, module: Any): Array<JsClassMetadata> {
        val res = mutableListOf<JsClassMetadata>()
        module.getOwnPropertyNames().forEach {

            val propName = it
            // indexed properties
            if (propName.all { it in '0'..'9' })
                return@forEach
            // closure functions
            if (propName.contains('$'))
                return@forEach
            if (propName == "prototype")
                return@forEach
            if (propName == "constructor")
                return@forEach

            // do not try to evaluate computable properties
            val descriptor = Object.getOwnPropertyDescriptor(module, propName).asDynamic()
            if (descriptor.value != null) {
                val propValue = module.asDynamic()[propName]
                if (propValue != null) {
                    if (propValue[KotlinCompiler.metadata] != null) {
                        // Kotlin Class
                        @Suppress("UNCHECKED_CAST_TO_NATIVE_INTERFACE")
                        res.add(JsClassMetadata((propValue as JsClass<*>).kotlin, module, pkg, propValue[KotlinCompiler.metadata]))
                    } else {
                        // Otherwise this is a new package
                        if (js("propValue instanceof Function"))
                            return@forEach
                        val pkgPlus = if (pkg == "") it else "${pkg}.${it}"
                        res.addAll(findAllClasses(pkgPlus, propValue))
                    }
                }
            }
        }

        return res.toTypedArray()
    }
}
