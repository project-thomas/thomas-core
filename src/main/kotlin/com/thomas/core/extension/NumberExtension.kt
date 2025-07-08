package com.thomas.core.extension

fun Int.isBetween(min: Int, max: Int): Boolean = this in min..max

fun Long.isHigher(value: Long): Boolean = this > value

fun Long.isLower(value: Long): Boolean = this < value