package com.thomas.core.model.security

import com.thomas.core.model.security.SecurityRole.ADMINISTRATOR_USER
import com.thomas.core.model.security.SecurityRole.MASTER_USER

abstract class SecurityInfo {

    companion object {
        private val MASTER_ROLES_SET = setOf(MASTER_USER)
        private val ADM_ROLES_SET = setOf(MASTER_USER, ADMINISTRATOR_USER)
    }

    abstract val securityRoles: Set<SecurityRole>

    val isMaster: Boolean
        get() = securityRoles.intersect(MASTER_ROLES_SET).isNotEmpty()

    val isAdministrator: Boolean
        get() = securityRoles.intersect(ADM_ROLES_SET).isNotEmpty()

}
