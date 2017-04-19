package kotlinx.coroutines.experimental

import kotlin.coroutines.experimental.*
import kotlin.js.Promise

suspend fun <T> Promise<T>.await() = suspendCoroutine<T> { cont ->
    then({ value -> cont.resume(value) },
        { exception -> cont.resumeWithException(exception) })
}

fun <T> async(block: suspend () -> T): Promise<T> = Promise<T> { resolve, reject ->
    block.startCoroutine(object : Continuation<T> {
        override val context: CoroutineContext get() = EmptyCoroutineContext
        override fun resume(value: T) { resolve(value) }
        override fun resumeWithException(exception: Throwable) { reject(exception) }
    })
}

fun launch(block: suspend () -> Unit) {
    async(block).catch { exception -> console.log("Failed with $exception") }
}