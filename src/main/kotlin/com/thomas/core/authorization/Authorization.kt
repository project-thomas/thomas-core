package com.thomas.core.authorization

import com.thomas.core.context.SessionContextHolder.currentUser
import com.thomas.core.model.security.SecurityRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

suspend fun <T> authorized(
    roles: Array<SecurityRole<*, *, *>> = arrayOf(),
    block: suspend CoroutineScope.() -> T
): T = coroutineScope {
    if (roles.isAuthorized()) {
        block()
    } else {
        throw UnauthorizedUserException()
    }
}

private fun Array<SecurityRole<*, *, *>>.isAuthorized(): Boolean =
    this@isAuthorized.isEmpty() || currentUser.currentRoles.intersect(this@isAuthorized.toSet()).isNotEmpty()
