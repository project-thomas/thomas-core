package com.thomas.core.model.general

import com.thomas.core.context.SessionContextHolder
import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.model.general.AddressRegion.CENTERWEST
import com.thomas.core.model.general.AddressRegion.NORTH
import com.thomas.core.model.general.AddressRegion.NORTHEAST
import com.thomas.core.model.general.AddressRegion.SOUTH
import com.thomas.core.model.general.AddressRegion.SOUTHEAST
import com.thomas.core.model.general.AddressRegion.SOUTHWEST
import com.thomas.core.model.general.AddressState.AC
import com.thomas.core.model.general.AddressState.AL
import com.thomas.core.model.general.AddressState.AM
import com.thomas.core.model.general.AddressState.AP
import com.thomas.core.model.general.AddressState.BA
import com.thomas.core.model.general.AddressState.CE
import com.thomas.core.model.general.AddressState.DF
import com.thomas.core.model.general.AddressState.ES
import com.thomas.core.model.general.AddressState.GO
import com.thomas.core.model.general.AddressState.MA
import com.thomas.core.model.general.AddressState.MG
import com.thomas.core.model.general.AddressState.MS
import com.thomas.core.model.general.AddressState.MT
import com.thomas.core.model.general.AddressState.PA
import com.thomas.core.model.general.AddressState.PB
import com.thomas.core.model.general.AddressState.PE
import com.thomas.core.model.general.AddressState.PI
import com.thomas.core.model.general.AddressState.PR
import com.thomas.core.model.general.AddressState.RJ
import com.thomas.core.model.general.AddressState.RN
import com.thomas.core.model.general.AddressState.RO
import com.thomas.core.model.general.AddressState.RR
import com.thomas.core.model.general.AddressState.RS
import com.thomas.core.model.general.AddressState.SC
import com.thomas.core.model.general.AddressState.SE
import com.thomas.core.model.general.AddressState.SP
import com.thomas.core.model.general.AddressState.TO
import java.util.Locale
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AddressStateTest {

    @AfterEach
    internal fun tearDown() {
        clearContext()
    }

    @Test
    fun `AddressState REGION`() {
        assertEquals(NORTH, AC.region)
        assertEquals(NORTHEAST, AL.region)
        assertEquals(NORTH, AP.region)
        assertEquals(NORTH, AM.region)
        assertEquals(NORTHEAST, BA.region)
        assertEquals(NORTHEAST, CE.region)
        assertEquals(CENTERWEST, DF.region)
        assertEquals(SOUTHWEST, ES.region)
        assertEquals(CENTERWEST, GO.region)
        assertEquals(NORTHEAST, MA.region)
        assertEquals(CENTERWEST, MT.region)
        assertEquals(CENTERWEST, MS.region)
        assertEquals(SOUTHEAST, MG.region)
        assertEquals(NORTH, PA.region)
        assertEquals(NORTHEAST, PB.region)
        assertEquals(SOUTH, PR.region)
        assertEquals(NORTHEAST, PE.region)
        assertEquals(NORTHEAST, PI.region)
        assertEquals(SOUTHEAST, RJ.region)
        assertEquals(NORTHEAST, RN.region)
        assertEquals(SOUTH, RS.region)
        assertEquals(NORTH, RO.region)
        assertEquals(NORTH, RR.region)
        assertEquals(SOUTH, SC.region)
        assertEquals(SOUTHEAST, SP.region)
        assertEquals(NORTHEAST, SE.region)
        assertEquals(NORTH, TO.region)
    }

    @Test
    fun `AddressState PT_BR`() {
        SessionContextHolder.currentLocale = Locale.forLanguageTag("pt-BR")
        assertEquals("Acre", AC.label)
        assertEquals("AC", AC.acronym)
        assertEquals("Alagoas", AL.label)
        assertEquals("AL", AL.acronym)
        assertEquals("Amapá", AP.label)
        assertEquals("AP", AP.acronym)
        assertEquals("Amazonas", AM.label)
        assertEquals("AM", AM.acronym)
        assertEquals("Bahia", BA.label)
        assertEquals("BA", BA.acronym)
        assertEquals("Ceará", CE.label)
        assertEquals("CE", CE.acronym)
        assertEquals("Distrito Federal", DF.label)
        assertEquals("DF", DF.acronym)
        assertEquals("Espírito Santo", ES.label)
        assertEquals("ES", ES.acronym)
        assertEquals("Goiás", GO.label)
        assertEquals("GO", GO.acronym)
        assertEquals("Maranhão", MA.label)
        assertEquals("MA", MA.acronym)
        assertEquals("Mato Grosso", MT.label)
        assertEquals("MT", MT.acronym)
        assertEquals("Mato Grosso do Sul", MS.label)
        assertEquals("MS", MS.acronym)
        assertEquals("Minas Gerais", MG.label)
        assertEquals("MG", MG.acronym)
        assertEquals("Pará", PA.label)
        assertEquals("PA", PA.acronym)
        assertEquals("Paraíba", PB.label)
        assertEquals("PB", PB.acronym)
        assertEquals("Paraná", PR.label)
        assertEquals("PR", PR.acronym)
        assertEquals("Pernambuco", PE.label)
        assertEquals("PE", PE.acronym)
        assertEquals("Piauí", PI.label)
        assertEquals("PI", PI.acronym)
        assertEquals("Rio de Janeiro", RJ.label)
        assertEquals("RJ", RJ.acronym)
        assertEquals("Rio Grande do Norte", RN.label)
        assertEquals("RN", RN.acronym)
        assertEquals("Rio Grande do Sul", RS.label)
        assertEquals("RS", RS.acronym)
        assertEquals("Rondônia", RO.label)
        assertEquals("RO", RO.acronym)
        assertEquals("Roraima", RR.label)
        assertEquals("RR", RR.acronym)
        assertEquals("Santa Catarina", SC.label)
        assertEquals("SC", SC.acronym)
        assertEquals("São Paulo", SP.label)
        assertEquals("SP", SP.acronym)
        assertEquals("Sergipe", SE.label)
        assertEquals("SE", SE.acronym)
        assertEquals("Tocantins", TO.label)
        assertEquals("TO", TO.acronym)
    }

    @Test
    fun `AddressState EN_US`() {
        SessionContextHolder.currentLocale = Locale.forLanguageTag("en-US")
        assertEquals("Acre", AC.label)
        assertEquals("AC", AC.acronym)
        assertEquals("Alagoas", AL.label)
        assertEquals("AL", AL.acronym)
        assertEquals("Amapá", AP.label)
        assertEquals("AP", AP.acronym)
        assertEquals("Amazonas", AM.label)
        assertEquals("AM", AM.acronym)
        assertEquals("Bahia", BA.label)
        assertEquals("BA", BA.acronym)
        assertEquals("Ceará", CE.label)
        assertEquals("CE", CE.acronym)
        assertEquals("Distrito Federal", DF.label)
        assertEquals("DF", DF.acronym)
        assertEquals("Espírito Santo", ES.label)
        assertEquals("ES", ES.acronym)
        assertEquals("Goiás", GO.label)
        assertEquals("GO", GO.acronym)
        assertEquals("Maranhão", MA.label)
        assertEquals("MA", MA.acronym)
        assertEquals("Mato Grosso", MT.label)
        assertEquals("MT", MT.acronym)
        assertEquals("Mato Grosso do Sul", MS.label)
        assertEquals("MS", MS.acronym)
        assertEquals("Minas Gerais", MG.label)
        assertEquals("MG", MG.acronym)
        assertEquals("Pará", PA.label)
        assertEquals("PA", PA.acronym)
        assertEquals("Paraíba", PB.label)
        assertEquals("PB", PB.acronym)
        assertEquals("Paraná", PR.label)
        assertEquals("PR", PR.acronym)
        assertEquals("Pernambuco", PE.label)
        assertEquals("PE", PE.acronym)
        assertEquals("Piauí", PI.label)
        assertEquals("PI", PI.acronym)
        assertEquals("Rio de Janeiro", RJ.label)
        assertEquals("RJ", RJ.acronym)
        assertEquals("Rio Grande do Norte", RN.label)
        assertEquals("RN", RN.acronym)
        assertEquals("Rio Grande do Sul", RS.label)
        assertEquals("RS", RS.acronym)
        assertEquals("Rondônia", RO.label)
        assertEquals("RO", RO.acronym)
        assertEquals("Roraima", RR.label)
        assertEquals("RR", RR.acronym)
        assertEquals("Santa Catarina", SC.label)
        assertEquals("SC", SC.acronym)
        assertEquals("São Paulo", SP.label)
        assertEquals("SP", SP.acronym)
        assertEquals("Sergipe", SE.label)
        assertEquals("SE", SE.acronym)
        assertEquals("Tocantins", TO.label)
        assertEquals("TO", TO.acronym)
    }

    @Test
    fun `AddressState ROOT`() {
        assertEquals("Acre", AC.label)
        assertEquals("AC", AC.acronym)
        assertEquals("Alagoas", AL.label)
        assertEquals("AL", AL.acronym)
        assertEquals("Amapá", AP.label)
        assertEquals("AP", AP.acronym)
        assertEquals("Amazonas", AM.label)
        assertEquals("AM", AM.acronym)
        assertEquals("Bahia", BA.label)
        assertEquals("BA", BA.acronym)
        assertEquals("Ceará", CE.label)
        assertEquals("CE", CE.acronym)
        assertEquals("Distrito Federal", DF.label)
        assertEquals("DF", DF.acronym)
        assertEquals("Espírito Santo", ES.label)
        assertEquals("ES", ES.acronym)
        assertEquals("Goiás", GO.label)
        assertEquals("GO", GO.acronym)
        assertEquals("Maranhão", MA.label)
        assertEquals("MA", MA.acronym)
        assertEquals("Mato Grosso", MT.label)
        assertEquals("MT", MT.acronym)
        assertEquals("Mato Grosso do Sul", MS.label)
        assertEquals("MS", MS.acronym)
        assertEquals("Minas Gerais", MG.label)
        assertEquals("MG", MG.acronym)
        assertEquals("Pará", PA.label)
        assertEquals("PA", PA.acronym)
        assertEquals("Paraíba", PB.label)
        assertEquals("PB", PB.acronym)
        assertEquals("Paraná", PR.label)
        assertEquals("PR", PR.acronym)
        assertEquals("Pernambuco", PE.label)
        assertEquals("PE", PE.acronym)
        assertEquals("Piauí", PI.label)
        assertEquals("PI", PI.acronym)
        assertEquals("Rio de Janeiro", RJ.label)
        assertEquals("RJ", RJ.acronym)
        assertEquals("Rio Grande do Norte", RN.label)
        assertEquals("RN", RN.acronym)
        assertEquals("Rio Grande do Sul", RS.label)
        assertEquals("RS", RS.acronym)
        assertEquals("Rondônia", RO.label)
        assertEquals("RO", RO.acronym)
        assertEquals("Roraima", RR.label)
        assertEquals("RR", RR.acronym)
        assertEquals("Santa Catarina", SC.label)
        assertEquals("SC", SC.acronym)
        assertEquals("São Paulo", SP.label)
        assertEquals("SP", SP.acronym)
        assertEquals("Sergipe", SE.label)
        assertEquals("SE", SE.acronym)
        assertEquals("Tocantins", TO.label)
        assertEquals("TO", TO.acronym)
    }

}
