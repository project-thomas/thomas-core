package com.thomas.core.model.general

import com.thomas.core.i18n.BundleResolver
import com.thomas.core.model.general.AddressState.AddressStateStringsI18N.coreAddressStateString
import com.thomas.core.model.general.AddressRegion.*

enum class AddressState(
    val region: AddressRegion
) {

    AC(NORTH),
    AL(NORTHEAST),
    AP(NORTH),
    AM(NORTH),
    BA(NORTHEAST),
    CE(NORTHEAST),
    DF(CENTERWEST),
    ES(SOUTHWEST),
    GO(CENTERWEST),
    MA(NORTHEAST),
    MT(CENTERWEST),
    MS(CENTERWEST),
    MG(SOUTHEAST),
    PA(NORTH),
    PB(NORTHEAST),
    PR(SOUTH),
    PE(NORTHEAST),
    PI(NORTHEAST),
    RJ(SOUTHEAST),
    RN(NORTHEAST),
    RS(SOUTH),
    RO(NORTH),
    RR(NORTH),
    SC(SOUTH),
    SP(SOUTHEAST),
    SE(NORTHEAST),
    TO(NORTH);

    val label: String
        get() = coreAddressStateString(this.name.lowercase(), "label")

    val acronym: String
        get() = coreAddressStateString(this.name.lowercase(), "acronym")

    private object AddressStateStringsI18N : BundleResolver("strings/core-state") {

        fun coreAddressStateString(name: String, property: String): String = formattedMessage("model.address-state.$name.$property")

    }

}
