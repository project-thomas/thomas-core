package com.thomas.core.model.general

import com.thomas.core.context.SessionContextHolder
import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.model.general.UserType.ADMINISTRATOR
import com.thomas.core.model.general.UserType.COMMON
import com.thomas.core.model.general.UserType.MASTER
import java.util.Locale
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserTypeTest {

    @AfterEach
    internal fun tearDown() {
        clearContext()
    }

    @Test
    fun `AddressState PT_BR`() {
        SessionContextHolder.currentLocale = Locale.forLanguageTag("pt-BR")
        assertEquals("Master", MASTER.typeName)
        assertEquals("Usuário master do sistema, com acesso a todas as funcionalidades", MASTER.typeDescription)
        assertEquals("Administrador", ADMINISTRATOR.typeName)
        assertEquals("Administrador com acesso total as funcionalidades da organização e unidades", ADMINISTRATOR.typeDescription)
        assertEquals("Comum", COMMON.typeName)
        assertEquals("Usuário comum com acesso a funcionalidades específicas", COMMON.typeDescription)
    }

    @Test
    fun `AddressState EN_US`() {
        SessionContextHolder.currentLocale = Locale.forLanguageTag("en-US")
        assertEquals("Master", MASTER.typeName)
        assertEquals("System master user, with access to all functionalities", MASTER.typeDescription)
        assertEquals("Administrator", ADMINISTRATOR.typeName)
        assertEquals("Administrator with full access to organization and unit features", ADMINISTRATOR.typeDescription)
        assertEquals("Common", COMMON.typeName)
        assertEquals("Common user with access to specific features", COMMON.typeDescription)
    }

    @Test
    fun `AddressState ROOT`() {
        assertEquals("Master", MASTER.typeName)
        assertEquals("Usuário master do sistema, com acesso a todas as funcionalidades", MASTER.typeDescription)
        assertEquals("Administrador", ADMINISTRATOR.typeName)
        assertEquals("Administrador com acesso total as funcionalidades da organização e unidades", ADMINISTRATOR.typeDescription)
        assertEquals("Comum", COMMON.typeName)
        assertEquals("Usuário comum com acesso a funcionalidades específicas", COMMON.typeDescription)
    }

}
