package com.thomas.core.model.general

import com.thomas.core.context.SessionContextHolder
import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.model.general.Race.*
import java.util.Locale
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RaceTest {

    @AfterEach
    internal fun tearDown() {
        clearContext()
    }

    @Test
    fun `Race PT_BR`() {
        SessionContextHolder.context.currentLocale = Locale.forLanguageTag("pt-BR")
        assertEquals("Branca", WHITE.typeName)
        assertEquals("Pessoas que se identificam com as características físicas e culturais associadas às populações europeias.", WHITE.typeDescription)
        assertEquals("Preta", BLACK.typeName)
        assertEquals("Pessoas que se identificam com as características físicas e culturais associadas às populações africanas.", BLACK.typeDescription)
        assertEquals("Amarela", YELLOW.typeName)
        assertEquals("Pessoas que se identificam com as características físicas e culturais associadas às populações asiáticas.", YELLOW.typeDescription)
        assertEquals("Parda", BROWN.typeName)
        assertEquals("Pessoas que se identificam com a miscigenação de diferentes raças, especialmente a mistura entre africanos e europeus.", BROWN.typeDescription)
        assertEquals("Indígena", INDIGENOUS.typeName)
        assertEquals("Pessoas que se identificam como pertencentes a grupos indígenas brasileiros.", INDIGENOUS.typeDescription)
    }

    @Test
    fun `Race EN_US`() {
        SessionContextHolder.context.currentLocale = Locale.forLanguageTag("en-US")
        assertEquals("White", WHITE.typeName)
        assertEquals("People who identify with the physical and cultural characteristics associated with European populations.", WHITE.typeDescription)
        assertEquals("Black", BLACK.typeName)
        assertEquals("People who identify with the physical and cultural characteristics associated with African populations.", BLACK.typeDescription)
        assertEquals("Yellow", YELLOW.typeName)
        assertEquals("People who identify with the physical and cultural characteristics associated with Asian populations.", YELLOW.typeDescription)
        assertEquals("Brown", BROWN.typeName)
        assertEquals("People who identify with the miscegenation of different races, especially the mixture between Africans and Europeans.", BROWN.typeDescription)
        assertEquals("Indigenous", INDIGENOUS.typeName)
        assertEquals("People who identify as belonging to Brazilian indigenous groups.", INDIGENOUS.typeDescription)
    }

    @Test
    fun `Race ROOT`() {
        assertEquals("Branca", WHITE.typeName)
        assertEquals("Pessoas que se identificam com as características físicas e culturais associadas às populações europeias.", WHITE.typeDescription)
        assertEquals("Preta", BLACK.typeName)
        assertEquals("Pessoas que se identificam com as características físicas e culturais associadas às populações africanas.", BLACK.typeDescription)
        assertEquals("Amarela", YELLOW.typeName)
        assertEquals("Pessoas que se identificam com as características físicas e culturais associadas às populações asiáticas.", YELLOW.typeDescription)
        assertEquals("Parda", BROWN.typeName)
        assertEquals("Pessoas que se identificam com a miscigenação de diferentes raças, especialmente a mistura entre africanos e europeus.", BROWN.typeDescription)
        assertEquals("Indígena", INDIGENOUS.typeName)
        assertEquals("Pessoas que se identificam como pertencentes a grupos indígenas brasileiros.", INDIGENOUS.typeDescription)
    }

}