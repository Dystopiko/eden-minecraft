package xyz.memothelemo.edenmc.utility

import xyz.memothelemo.edenmc.EdenMod
import xyz.memothelemo.edenmc.api.Eden
import xyz.memothelemo.edenmc.api.EdenProvider
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
        EdenMod.logger.debug("Successfully injected EvenProvider with EdenImpl")
    } catch (ex: Exception) {
        EdenMod.logger.warn("Could not inject EdenProvider with the implementation class", ex)
    }

}
