package com.thomas.core.aspect

interface AspectSerializer {

    fun serialize(value: Any?, masked: Boolean): String

}
