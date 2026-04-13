package com.github.dystopiko.edenmc.utility

import com.github.dystopiko.edenmc.EdenMod
import com.github.dystopiko.edenmc.api.Eden
import com.github.dystopiko.edenmc.api.EdenProvider
import java.lang.reflect.Method

fun injectApiImpl(impl: Eden) {
    var method: Method? = null
    try {
        method = EdenProvider::class.java.getDeclaredMethod("register", Eden::class.java)
        method.isAccessible = true
    } catch (ex: Exception) {
        throw IllegalStateException("Cannot find EdenProvider::register method", ex)
    }

    try {
        method.invoke(impl)
    } catch (ex: Exception) {
        EdenMod.logger.warn("Could not inject EdenProvider with the implementation class", ex)
    }
}
