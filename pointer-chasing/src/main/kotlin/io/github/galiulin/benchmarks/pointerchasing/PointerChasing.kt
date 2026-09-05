@file:JvmName("PointerChasing")

package io.github.galiulin.benchmarks.pointerchasing

import kotlin.random.Random

/**
 * Code that checks the claims from the "Pointer chasing" note with JMH
 * microbenchmarks. It lives in main only so the same classes are visible from
 * both the test (correctness) and jmh (performance) source sets, without
 * duplication.
 */

/** A singly linked list node: the address of next can only be read, not computed. */
class Node(@JvmField val value: Long, @JvmField var next: Node? = null)

fun buildArray(size: Int): LongArray = LongArray(size) { it.toLong() }

/**
 * The N nodes are created in order (0..size-1) and linked in the same order: next
 * always points to the physically adjacent object (under bump-pointer allocation).
 * This is the "friendly" linked list: the address of the next node correlates with
 * the current one almost like in an array, just with one extra dereference.
 */
fun buildSequentialLinkedList(size: Int): Node {
    require(size > 0)
    val nodes = Array(size) { Node(it.toLong()) }
    for (i in 0 until size - 1) nodes[i].next = nodes[i + 1]
    return nodes[0]
}

/**
 * The nodes are created in the same order 0..size-1 (the same physical addresses as
 * in [buildSequentialLinkedList]), but next links them in random order: a single
 * cycle of length size, so the traversal visits every node exactly once. The
 * address of the next node no longer correlates with the current one, so the
 * prefetcher is helpless, exactly as the note describes.
 */
fun buildShuffledLinkedList(size: Int, seed: Long): Node {
    require(size > 0)
    val nodes = Array(size) { Node(it.toLong()) }
    val order = IntArray(size) { it }
    val random = Random(seed)
    for (i in size - 1 downTo 1) {
        val j = random.nextInt(i + 1)
        val tmp = order[i]
        order[i] = order[j]
        order[j] = tmp
    }
    for (i in 0 until size - 1) nodes[order[i]].next = nodes[order[i + 1]]
    return nodes[order[0]]
}

/**
 * ArrayList<Long> is also an "array", but its elements are boxed: the backing
 * array holds references, and each Long is a separate object on the heap. A
 * middle case between sumArray and sumLinked: there's a dereference, but box
 * addresses are usually sequential (allocated back to back while filling the list).
 */
fun buildBoxedList(size: Int): List<Long> {
    val list = ArrayList<Long>(size)
    for (i in 0 until size) list.add(i.toLong())
    return list
}

fun sumArray(values: LongArray): Long {
    var sum = 0L
    for (v in values) sum += v // addresses are known ahead of time
    return sum
}

fun sumLinked(head: Node?): Long {
    var sum = 0L
    var n = head
    while (n != null) {
        sum += n.value // the address of the next node is only known now
        n = n.next
    }
    return sum
}

fun sumBoxed(values: List<Long>): Long {
    var sum = 0L
    for (v in values) sum += v
    return sum
}
