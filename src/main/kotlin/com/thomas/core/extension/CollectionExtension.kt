package com.thomas.core.extension

fun <T> MutableList<T>.addIf(
    element: T,
    check: (T) -> Boolean
) = if (check(element)) {
    this.add(element)
} else {
    false
}

fun <T> MutableList<T>.addIfAbsent(
    element: T
) = this.addIf(element) { e -> !this.any { it == e } }

fun <K, V> Map<K, Set<V>>.merge(map: Map<K, Set<V>>) =
    (this.keys + map.keys).distinct().associateWith {
        (this[it] ?: emptySet()) + (map[it] ?: emptySet()).distinct().toSet()
    }
