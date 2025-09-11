package com.thomas.core.extension

import java.text.Normalizer
import java.util.UUID

@Suppress("MaxLineLength")
const val ACCENTS_LOWER_REGEX_VALUE =
    "aàáâãäạảăắặằẳẵấậầẩẫåāącçčćĉċdđðďeèéêëẹẻẽếệềểễēĕėęěgĝğġģhĥħiìíîïịỉĩīĭį̇ıjĵkķĸlĺļľŀłnñńņňŉŋoòóôõöọỏốộồổỗơớợờởỡøōŏőŕŗřsšśŝştţťŧuùúûüụủũưứựừửữŭūůűųwŵyýÿỵỳỷỹŷzžźż"

@Suppress("MaxLineLength")
const val ACCENTS_UPPER_REGEX_VALUE =
    "AÀÁÂÃÄẠẢĂẮẶẰẲẴẤẬẦẨẪÅĀĄCÇČĆĈĊDĐÐĎEÈÉÊËẸẺẼẾỆỀỂỄĒĔĖĘĚGĜĞĠĢHĤĦIÌÍÎÏỊỈĨĪĬĮ̇IJĴKĶĸLĹĻĽĿŁNÑŃŅŇNŊOÒÓÔÕÖỌỎỐỘỒỔỖƠỚỢỜỞỠØŌŎŐŔŖŘSŠŚŜŞTŢŤŦUÙÚÛÜỤỦŨƯỨỰỪỬỮŬŪŮŰŲWŴYÝŸỴỲỶỸŶZŽŹŻ"
const val LETTERS_ONLY_REGEX_VALUE = "A-Za-z$ACCENTS_LOWER_REGEX_VALUE$ACCENTS_UPPER_REGEX_VALUE"

val EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$".toRegex()

val UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$".toRegex()

fun String.onlyNumbers() = this.replace("[^0-9]".toRegex(), "")

fun String.onlyLettersAndNumbers() = this.filter { it.isLetterOrDigit() }

fun String.toUUIDOrNull(): UUID? = this.takeIf { it.matches(UUID_REGEX) }?.let { UUID.fromString(it) }

fun String.toSnakeCase(): String = this.replace("(?<=.)[A-Z]".toRegex(), "_$0").lowercase()

fun String.unaccented() = Normalizer.normalize(this, Normalizer.Form.NFD)
    .replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")

fun String.unaccentedLower() = this.unaccented().lowercase()
