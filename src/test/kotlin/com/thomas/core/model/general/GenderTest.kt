package com.thomas.core.model.general

import com.thomas.core.context.SessionContextHolder
import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.model.general.Gender.ANDROGYNOUS_GENDER
import com.thomas.core.model.general.Gender.BI_GENDER
import com.thomas.core.model.general.Gender.CIS_FEMALE
import com.thomas.core.model.general.Gender.CIS_MALE
import com.thomas.core.model.general.Gender.FEMALE_TRANSGENDER
import com.thomas.core.model.general.Gender.FEMALE_TRANSSEXUAL
import com.thomas.core.model.general.Gender.MALE_TRANSGENDER
import com.thomas.core.model.general.Gender.MALE_TRANSSEXUAL
import com.thomas.core.model.general.Gender.NEUTRAL_GENDER
import com.thomas.core.model.general.Gender.NON_BINARY
import com.thomas.core.model.general.Gender.NO_GENDER
import com.thomas.core.model.general.Gender.OTHER_GENDER
import com.thomas.core.model.general.Gender.TRANS_FEMALE
import com.thomas.core.model.general.Gender.TRANS_MALE
import java.util.Locale
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GenderTest {

    @AfterEach
    internal fun tearDown() {
        clearContext()
    }

    @Test
    fun `Genders PT_BR`() {
        SessionContextHolder.context.currentLocale = Locale.forLanguageTag("pt-BR")
        assertEquals("Masculino", CIS_MALE.label)
        assertEquals("Feminino", CIS_FEMALE.label)
        assertEquals("Agênero", NO_GENDER.label)
        assertEquals("Andrógino", ANDROGYNOUS_GENDER.label)
        assertEquals("Bigênero", BI_GENDER.label)
        assertEquals("Não binário", NON_BINARY.label)
        assertEquals("Neutro", NEUTRAL_GENDER.label)
        assertEquals("Trans Masculino", TRANS_MALE.label)
        assertEquals("Trans Feminino", TRANS_FEMALE.label)
        assertEquals("Transgênero Masculino", MALE_TRANSGENDER.label)
        assertEquals("Transgênero Feminino", FEMALE_TRANSGENDER.label)
        assertEquals("Transsexual Masculino", MALE_TRANSSEXUAL.label)
        assertEquals("Transsexual Feminino", FEMALE_TRANSSEXUAL.label)
        assertEquals("Outro", OTHER_GENDER.label)
    }

    @Test
    fun `Genders EN_US`() {
        SessionContextHolder.context.currentLocale = Locale.forLanguageTag("en-US")
        assertEquals("Male", CIS_MALE.label)
        assertEquals("Female", CIS_FEMALE.label)
        assertEquals("Agender", NO_GENDER.label)
        assertEquals("Androgynous", ANDROGYNOUS_GENDER.label)
        assertEquals("Bigender", BI_GENDER.label)
        assertEquals("Non-binary", NON_BINARY.label)
        assertEquals("Neutral", NEUTRAL_GENDER.label)
        assertEquals("Trans Male", TRANS_MALE.label)
        assertEquals("Trans Female", TRANS_FEMALE.label)
        assertEquals("Transgender Male", MALE_TRANSGENDER.label)
        assertEquals("Transgender Female", FEMALE_TRANSGENDER.label)
        assertEquals("Transsexual Male", MALE_TRANSSEXUAL.label)
        assertEquals("Transsexual Female", FEMALE_TRANSSEXUAL.label)
        assertEquals("Other", OTHER_GENDER.label)
    }

    @Test
    fun `Genders CH`() {
        SessionContextHolder.context.currentLocale = Locale.CHINA
        assertEquals("Masculino", CIS_MALE.label)
        assertEquals("Feminino", CIS_FEMALE.label)
        assertEquals("Agênero", NO_GENDER.label)
        assertEquals("Andrógino", ANDROGYNOUS_GENDER.label)
        assertEquals("Bigênero", BI_GENDER.label)
        assertEquals("Não binário", NON_BINARY.label)
        assertEquals("Neutro", NEUTRAL_GENDER.label)
        assertEquals("Trans Masculino", TRANS_MALE.label)
        assertEquals("Trans Feminino", TRANS_FEMALE.label)
        assertEquals("Transgênero Masculino", MALE_TRANSGENDER.label)
        assertEquals("Transgênero Feminino", FEMALE_TRANSGENDER.label)
        assertEquals("Transsexual Masculino", MALE_TRANSSEXUAL.label)
        assertEquals("Transsexual Feminino", FEMALE_TRANSSEXUAL.label)
        assertEquals("Outro", OTHER_GENDER.label)
    }

    @Test
    fun `Genders ROOT`() {
        assertEquals("Masculino", CIS_MALE.label)
        assertEquals("Feminino", CIS_FEMALE.label)
        assertEquals("Agênero", NO_GENDER.label)
        assertEquals("Andrógino", ANDROGYNOUS_GENDER.label)
        assertEquals("Bigênero", BI_GENDER.label)
        assertEquals("Não binário", NON_BINARY.label)
        assertEquals("Neutro", NEUTRAL_GENDER.label)
        assertEquals("Trans Masculino", TRANS_MALE.label)
        assertEquals("Trans Feminino", TRANS_FEMALE.label)
        assertEquals("Transgênero Masculino", MALE_TRANSGENDER.label)
        assertEquals("Transgênero Feminino", FEMALE_TRANSGENDER.label)
        assertEquals("Transsexual Masculino", MALE_TRANSSEXUAL.label)
        assertEquals("Transsexual Feminino", FEMALE_TRANSSEXUAL.label)
        assertEquals("Outro", OTHER_GENDER.label)
    }

}
