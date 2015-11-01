@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("MapsKt")

package kotlin.collections

//
// NOTE THIS FILE IS AUTO-GENERATED by the GenerateStandardLib.kt
// See: https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib
//

import java.util.*

import java.util.Collections // TODO: it's temporary while we have java.util.Collections in js

/**
 * Returns a [List] containing all key-value pairs.
 */
public fun <K, V> Map<K, V>.toList(): List<Pair<K, V>> {
    val result = ArrayList<Pair<K, V>>(size)
    for (item in this)
        result.add(item.key to item.value)
    return result
}

/**
 * Returns a single list of all elements yielded from results of [transform] function being invoked on each entry of original map.
 */
public inline fun <K, V, R> Map<K, V>.flatMap(transform: (Map.Entry<K, V>) -> Iterable<R>): List<R> {
    return flatMapTo(ArrayList<R>(), transform)
}

/**
 * Appends all elements yielded from results of [transform] function being invoked on each entry of original map, to the given [destination].
 */
public inline fun <K, V, R, C : MutableCollection<in R>> Map<K, V>.flatMapTo(destination: C, transform: (Map.Entry<K, V>) -> Iterable<R>): C {
    for (element in this) {
        val list = transform(element)
        destination.addAll(list)
    }
    return destination
}

/**
 * Returns a list containing the results of applying the given [transform] function
 * to each entry in the original map.
 */
public inline fun <K, V, R> Map<K, V>.map(transform: (Map.Entry<K, V>) -> R): List<R> {
    return mapTo(ArrayList<R>(size), transform)
}

/**
 * Applies the given [transform] function to each entry and its index in the original map
 * and appends the results to the given [destination].
 */
@Deprecated("Use entries.mapIndexedTo instead.", ReplaceWith("this.entries.mapIndexedTo(destination, transform)"))
public inline fun <K, V, R, C : MutableCollection<in R>> Map<K, V>.mapIndexedTo(destination: C, transform: (Int, Map.Entry<K, V>) -> R): C {
    var index = 0
    for (item in this)
        destination.add(transform(index++, item))
    return destination
}

/**
 * Returns a list containing only the non-null results of applying the given [transform] function
 * to each entry in the original map.
 */
public inline fun <K, V, R : Any> Map<K, V>.mapNotNull(transform: (Map.Entry<K, V>) -> R?): List<R> {
    return mapNotNullTo(ArrayList<R>(), transform)
}

/**
 * Applies the given [transform] function to each entry in the original map
 * and appends only the non-null results to the given [destination].
 */
public inline fun <K, V, R : Any, C : MutableCollection<in R>> Map<K, V>.mapNotNullTo(destination: C, transform: (Map.Entry<K, V>) -> R?): C {
    forEach { element -> transform(element)?.let { destination.add(it) } }
    return destination
}

/**
 * Applies the given [transform] function to each entry of the original map
 * and appends the results to the given [destination].
 */
public inline fun <K, V, R, C : MutableCollection<in R>> Map<K, V>.mapTo(destination: C, transform: (Map.Entry<K, V>) -> R): C {
    for (item in this)
        destination.add(transform(item))
    return destination
}

/**
 * Returns `true` if all entries match the given [predicate].
 */
public inline fun <K, V> Map<K, V>.all(predicate: (Map.Entry<K, V>) -> Boolean): Boolean {
    for (element in this) if (!predicate(element)) return false
    return true
}

/**
 * Returns `true` if map has at least one entry.
 */
public fun <K, V> Map<K, V>.any(): Boolean {
    for (element in this) return true
    return false
}

/**
 * Returns `true` if at least one entry matches the given [predicate].
 */
public inline fun <K, V> Map<K, V>.any(predicate: (Map.Entry<K, V>) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}

/**
 * Returns the number of entries in this map.
 */
public fun <K, V> Map<K, V>.count(): Int {
    return size
}

/**
 * Returns the number of entries matching the given [predicate].
 */
public inline fun <K, V> Map<K, V>.count(predicate: (Map.Entry<K, V>) -> Boolean): Int {
    var count = 0
    for (element in this) if (predicate(element)) count++
    return count
}

/**
 * Performs the given [action] on each entry.
 */
public inline fun <K, V> Map<K, V>.forEach(action: (Map.Entry<K, V>) -> Unit): Unit {
    for (element in this) action(element)
}

/**
 * Returns the first map entry yielding the largest value of the given function or `null` if there are no entries.
 */
public inline fun <K, V, R : Comparable<R>> Map<K, V>.maxBy(selector: (Map.Entry<K, V>) -> R): Map.Entry<K, V>? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var maxElem = iterator.next()
    var maxValue = selector(maxElem)
    while (iterator.hasNext()) {
        val e = iterator.next()
        val v = selector(e)
        if (maxValue < v) {
            maxElem = e
            maxValue = v
        }
    }
    return maxElem
}

/**
 * Returns the first map entry yielding the smallest value of the given function or `null` if there are no entries.
 */
public inline fun <K, V, R : Comparable<R>> Map<K, V>.minBy(selector: (Map.Entry<K, V>) -> R): Map.Entry<K, V>? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var minElem = iterator.next()
    var minValue = selector(minElem)
    while (iterator.hasNext()) {
        val e = iterator.next()
        val v = selector(e)
        if (minValue > v) {
            minElem = e
            minValue = v
        }
    }
    return minElem
}

/**
 * Returns `true` if the map has no entries.
 */
public fun <K, V> Map<K, V>.none(): Boolean {
    for (element in this) return false
    return true
}

/**
 * Returns `true` if no entries match the given [predicate].
 */
public inline fun <K, V> Map<K, V>.none(predicate: (Map.Entry<K, V>) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return false
    return true
}

/**
 * Creates an [Iterable] instance that wraps the original map returning its entries when being iterated.
 */
public fun <K, V> Map<K, V>.asIterable(): Iterable<Map.Entry<K, V>> {
    return entries
}

/**
 * Creates a [Sequence] instance that wraps the original map returning its entries when being iterated.
 */
public fun <K, V> Map<K, V>.asSequence(): Sequence<Map.Entry<K, V>> {
    return object : Sequence<Map.Entry<K, V>> {
        override fun iterator(): Iterator<Map.Entry<K, V>> {
            return this@asSequence.iterator()
        }
    }
}

