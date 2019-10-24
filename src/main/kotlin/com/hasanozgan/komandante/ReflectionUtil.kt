package com.hasanozgan.komandante

import arrow.data.extensions.list.foldable.exists
import java.lang.reflect.Method

fun <T : Message> getMethod(obj: Any, name: String, message: T): Method? {
    return obj.javaClass.methods.filter {
        it.name.equals(name) && it.parameterTypes.toList().exists {
            it.equals(message.javaClass)
        }
    }.firstOrNull()
}