package com.thomas.core.aspect

internal class AspectToStringSerializer : AspectSerializer {

    companion object {
        private const val MASKED_VALUE_PLACEHOLDER = "**********"
        private const val NULL_VALUE_PLACEHOLDER = "null"
    }

    override fun serialize(
        value: Any?,
        masked: Boolean,
    ): String = value?.let { v ->
        v.takeIf {
            it is Throwable
        }?.let {
            "${it.javaClass.simpleName}(${(it as Throwable).message})"
        } ?: (MASKED_VALUE_PLACEHOLDER.takeIf { masked } ?: v.toString())
    } ?: NULL_VALUE_PLACEHOLDER

}
