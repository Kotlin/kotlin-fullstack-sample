package runtime.reactive

/**
 * Classical Maybe monad
 */
sealed class Maybe<out T> {
    object None : Maybe<Nothing>() {
        override fun equals(other: Any?) = other === None
        override fun hashCode(): Int = -1
        override fun toString(): String = "Maybe.None"
    }

    class Just<T>(val value: T) : Maybe<T>() {
        override fun equals(other: Any?) = (other as? Just<*>)?.value?.equals(value) ?: false
        override fun hashCode(): Int = value?.hashCode() ?: 0
        override fun toString(): String = "Maybe.Just($value)"

    }

    val hasValue: Boolean get() = this is Just

    val asNullable: T?
        get() = when (this) {
            is None -> null
            is Just -> value
        }

    fun orElseThrow(): T {
        return orElseThrow({ IllegalStateException("Monad is Empty!") })
    }

    fun orElseThrow(err: () -> IllegalStateException): T {
        when (this) {
            is None -> throw err()
            is Just -> return value
        }
    }

    override fun toString(): String {
        if (hasValue) {
            return (this as Just).value.toString()
        } else {
            return "<null>"
        }
    }
}

fun <T : Any> Maybe<T>.asNullable(): T? {
    return when (this) {
        is Maybe.None -> null
        is Maybe.Just -> value
    }
}

fun <T, U> Maybe<T>.map(converter: (T) -> U): Maybe<U> {
    return when (this) {
        is Maybe.None -> Maybe.None
        is Maybe.Just -> Maybe.Just(converter(value))
    }
}

fun <T, U> Maybe<T>.flatMap(converter: (T) -> Maybe<U>): Maybe<U> {
    return when (this) {
        is Maybe.None -> Maybe.None
        is Maybe.Just -> converter(value)
    }
}

fun <T> Maybe<T>.orElse(fallback: T): T {
    return when (this) {
        is Maybe.None -> fallback
        is Maybe.Just -> this.value
    }
}

fun <T> Maybe<T>.orElseMaybe(fallback: Maybe<T>): Maybe<T> {
    return when (this) {
        is Maybe.None -> fallback
        is Maybe.Just -> this
    }
}

/**
 * Classical Result monad
 */
sealed class Result<out T> {
    companion object {
        inline fun <T> wrap(action: () -> T): Result<T> {
            try {
                return Success(action())
            } catch (t: Throwable) {
                return Failure(t)
            }
        }
    }

    class Success<T>(val value: T) : Result<T>()
    class Failure(val error: Throwable) : Result<Nothing>()

    inline fun <E> transform(onSuccess: (T) -> E, onFailure: (Throwable) -> E): E {
        return when (this) {
            is Success -> onSuccess(value)
            is Failure -> onFailure(error)
        }
    }
}
