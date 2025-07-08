package com.thomas.core.model.general

import com.thomas.core.i18n.BundleResolver
import com.thomas.core.model.general.AddressRegion.AddressRegionStringsI18N.coreAddressRegionString

enum class AddressRegion {

    NORTH,
    SOUTH,
    EAST,
    WEST,
    NORTHEAST,
    NORTHWEST,
    SOUTHEAST,
    SOUTHWEST,
    CENTERWEST;

    val label: String
        get() = coreAddressRegionString(this.name.lowercase())

    private object AddressRegionStringsI18N : BundleResolver("strings/core-region") {

        fun coreAddressRegionString(name: String): String = formattedMessage("model.address-region.$name.label")

    }

}