package com.thomas.core.extension

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

internal fun Type.simpleTypedName(): String = when (this) {
    is Class<*> -> this.simpleName

    is ParameterizedType -> {
        val rawType = (this.rawType as Class<*>).simpleName
        val typeArgs = this.actualTypeArguments.map { it.simpleTypedName() }
        if (typeArgs.isNotEmpty()) {
            "$rawType<${typeArgs.joinToString(", ")}>"
        } else {
            rawType
        }
    }

    is GenericArrayType -> {
        val componentType = this.genericComponentType.simpleTypedName()
        "$componentType[]"
    }

    is TypeVariable<*> -> {
        this.name
    }

    is WildcardType -> {
        val upperBounds = this.upperBounds
        val lowerBounds = this.lowerBounds

        when {
            lowerBounds.isNotEmpty() -> lowerBounds[0].simpleTypedName()
            upperBounds.isNotEmpty() && upperBounds[0] != Any::class.java -> upperBounds[0].simpleTypedName()
            else -> "Any"
        }
    }

    else -> this.toString().substringAfterLast('.')
}
