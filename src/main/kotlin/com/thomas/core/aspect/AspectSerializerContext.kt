package com.thomas.core.aspect

import java.util.concurrent.atomic.AtomicReference

object AspectSerializerContext {

    private val serializerRef = AtomicReference<AspectSerializer>(AspectToStringSerializer())

    @JvmStatic
    var aspectSerializer: AspectSerializer
        get() = serializerRef.get()
        set(value) = serializerRef.set(value)

}
