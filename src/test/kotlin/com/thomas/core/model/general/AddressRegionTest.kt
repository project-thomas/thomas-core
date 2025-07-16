package com.thomas.core.model.general

import com.thomas.core.context.SessionContextHolder
import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.model.general.AddressRegion.CENTERWEST
import com.thomas.core.model.general.AddressRegion.EAST
import com.thomas.core.model.general.AddressRegion.NORTH
import com.thomas.core.model.general.AddressRegion.NORTHEAST
import com.thomas.core.model.general.AddressRegion.NORTHWEST
import com.thomas.core.model.general.AddressRegion.SOUTH
import com.thomas.core.model.general.AddressRegion.SOUTHEAST
import com.thomas.core.model.general.AddressRegion.SOUTHWEST
import com.thomas.core.model.general.AddressRegion.WEST
import java.util.Locale
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AddressRegionTest {

    @AfterEach
    internal fun tearDown() {
        clearContext()
    }

    @Test
    fun `AddressRegion PT_BR`() {
        SessionContextHolder.context.currentLocale = Locale.forLanguageTag("pt-BR")
        assertEquals("Norte", NORTH.label)
        assertEquals("Sul", SOUTH.label)
        assertEquals("Leste", EAST.label)
        assertEquals("Oeste", WEST.label)
        assertEquals("Nordeste", NORTHEAST.label)
        assertEquals("Noroeste", NORTHWEST.label)
        assertEquals("Sudeste", SOUTHEAST.label)
        assertEquals("Sudoeste", SOUTHWEST.label)
        assertEquals("Centro-Oeste", CENTERWEST.label)
    }

    @Test
    fun `AddressRegion EN_US`() {
        SessionContextHolder.context.currentLocale = Locale.forLanguageTag("en-US")
        assertEquals("North", NORTH.label)
        assertEquals("South", SOUTH.label)
        assertEquals("East", EAST.label)
        assertEquals("West", WEST.label)
        assertEquals("Northeast", NORTHEAST.label)
        assertEquals("Northwest", NORTHWEST.label)
        assertEquals("Southeast", SOUTHEAST.label)
        assertEquals("Southwest", SOUTHWEST.label)
        assertEquals("Center-West", CENTERWEST.label)
    }

    @Test
    fun `AddressRegion ROOT`() {
        assertEquals("Norte", NORTH.label)
        assertEquals("Sul", SOUTH.label)
        assertEquals("Leste", EAST.label)
        assertEquals("Oeste", WEST.label)
        assertEquals("Nordeste", NORTHEAST.label)
        assertEquals("Noroeste", NORTHWEST.label)
        assertEquals("Sudeste", SOUTHEAST.label)
        assertEquals("Sudoeste", SOUTHWEST.label)
        assertEquals("Centro-Oeste", CENTERWEST.label)
    }

}