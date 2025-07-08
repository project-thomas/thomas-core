package com.thomas.core.model.general

import com.thomas.core.i18n.BundleResolver
import com.thomas.core.model.general.Gender.GenderStringsI18N.coreGenderString

enum class Gender {
    CIS_MALE,
    CIS_FEMALE,
    NO_GENDER,
    ANDROGYNOUS_GENDER,
    BI_GENDER,
    NON_BINARY,
    NEUTRAL_GENDER,
    TRANS_MALE,
    TRANS_FEMALE,
    MALE_TRANSGENDER,
    FEMALE_TRANSGENDER,
    MALE_TRANSSEXUAL,
    FEMALE_TRANSSEXUAL,
    OTHER_GENDER;

    val label: String
        get() = coreGenderString(this.name.lowercase())

    private object GenderStringsI18N : BundleResolver("strings/core-genders") {

        fun coreGenderString(name: String): String = formattedMessage("model.gender.$name.label")

    }

}
