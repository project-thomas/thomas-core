package com.thomas.core.aspect

internal class AspectToStringSerializer : AspectSerializer {

    companion object {
        private const val MASKED_VALUE_PLACEHOLDER = "**********"
        private const val NULL_VALUE_PLACEHOLDER = "null"
    }

    override fun serialize(
        value: Any?,
        masked: Boolean,
    ): String = when {
        value == null -> NULL_VALUE_PLACEHOLDER
        value is Throwable -> formatThrowable(value)
        masked -> MASKED_VALUE_PLACEHOLDER
        else -> value.toString()
    }

    private fun formatThrowable(
        throwable: Throwable
    ): String = "${throwable.javaClass.simpleName}(${throwable.message})"

}
