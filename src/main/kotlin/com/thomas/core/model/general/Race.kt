package com.thomas.core.model.general

import com.thomas.core.i18n.BundleResolver
import com.thomas.core.model.general.Race.RaceStringsI18N.coreRaceDescription
import com.thomas.core.model.general.Race.RaceStringsI18N.coreRaceName

enum class Race {

    WHITE,
    BLACK,
    YELLOW,
    BROWN,
    INDIGENOUS;

    val typeName: String
        get() = coreRaceName(this.name.lowercase())

    val typeDescription: String
        get() = coreRaceDescription(this.name.lowercase())

    object RaceStringsI18N : BundleResolver("strings/core-races") {

        fun coreRaceName(
            name: String
        ): String = coreRacesString(name, "name")

        fun coreRaceDescription(
            name: String
        ): String = coreRacesString(name, "description")

        private fun coreRacesString(
            name: String,
            attribute: String
        ): String = formattedMessage("model.race.$name.$attribute")

    }

}