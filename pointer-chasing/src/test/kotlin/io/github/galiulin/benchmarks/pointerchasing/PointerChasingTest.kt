package io.github.galiulin.benchmarks.pointerchasing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Correctness of the structures/functions that JMH later runs in src/jmh. Cheap
 * runs on a small N: they say nothing about performance on their own, only that
 * sumArray/sumLinked/sumBoxed and the three builders agree on the result and
 * visit every node exactly once.
 */
class PointerChasingTest : FunSpec({

    fun expectedSum(size: Int): Long = (0 until size).sumOf { it.toLong() }

    context("sumArray") {
        test("sums a LongArray") {
            sumArray(buildArray(1000)) shouldBe expectedSum(1000)
        }

        test("returns 0 for an empty array") {
            sumArray(LongArray(0)) shouldBe 0L
        }
    }

    context("sumLinked") {
        test("sequential list: sum matches sumArray") {
            sumLinked(buildSequentialLinkedList(1000)) shouldBe expectedSum(1000)
        }

        test("null head returns sum 0") {
            sumLinked(null) shouldBe 0L
        }

        test("shuffled list visits every node exactly once") {
            val size = 5000
            val head = buildShuffledLinkedList(size, seed = 42L)

            sumLinked(head) shouldBe expectedSum(size)
        }

        test("shuffling with the same seed is deterministic") {
            val a = buildShuffledLinkedList(2000, seed = 7L)
            val b = buildShuffledLinkedList(2000, seed = 7L)

            sumLinked(a) shouldBe sumLinked(b)
        }

        test("shuffling is not the identity order") {
            // With 10,000 nodes, the odds that Fisher-Yates happens to return the
            // original 0..N-1 order are practically zero.
            val size = 10_000
            val sequentialValues = mutableListOf<Long>()
            var seqNode: Node? = buildSequentialLinkedList(size)
            while (seqNode != null) {
                sequentialValues += seqNode.value
                seqNode = seqNode.next
            }

            val shuffledValues = mutableListOf<Long>()
            var shufNode: Node? = buildShuffledLinkedList(size, seed = 1L)
            while (shufNode != null) {
                shuffledValues += shufNode.value
                shufNode = shufNode.next
            }

            (shuffledValues == sequentialValues) shouldBe false
        }
    }

    context("sumBoxed") {
        test("ArrayList<Long>: sum matches sumArray") {
            sumBoxed(buildBoxedList(1000)) shouldBe expectedSum(1000)
        }

        test("returns 0 for an empty list") {
            sumBoxed(emptyList()) shouldBe 0L
        }
    }
})
