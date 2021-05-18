package com.vbank.vidyovideoview.event

open class BusBase<T, Call : CallBase?> @JvmOverloads constructor(
    open val call: Call,
    private val values: Array<out T>? = null
) {

    fun getValue(): T? {
        return if (values == null || values.isEmpty()) null else values[0]
    }

    fun hasValues(): Boolean {
        return getValue() != null
    }

    override fun toString(): String {
        return "BusBase{call=$call}"
    }

}