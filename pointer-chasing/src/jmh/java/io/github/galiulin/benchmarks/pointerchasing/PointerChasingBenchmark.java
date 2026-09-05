package io.github.galiulin.benchmarks.pointerchasing;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Checks the claims from the "Pointer chasing" note: compares four ways to sum N
 * numbers that differ only in memory access pattern, not algorithm. All four are
 * O(n), one pass, one accumulator.
 *
 * <ul>
 *   <li>{@link #array}          — LongArray, addresses are predictable (baseline).</li>
 *   <li>{@link #linkedSequential} — a linked list whose nodes are allocated and
 *       linked in the same order: addresses are almost like the array's, just with
 *       an extra dereference.</li>
 *   <li>{@link #linkedShuffled}  — the same nodes, but next links them in random
 *       order: the address of the next node no longer correlates with the current
 *       one. This is the pointer chasing the note is about.</li>
 *   <li>{@link #boxedArrayList}  — ArrayList&lt;Long&gt;: also an "array", but
 *       boxed. A middle ground between array and linkedShuffled.</li>
 * </ul>
 *
 * Java, not Kotlin: champeau.jmh runs JMH's annotation processor through javac's
 * APT, and a Java source set picks that up without kapt.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgs = {"-Xms1g", "-Xmx4g"})
public class PointerChasingBenchmark {

    /**
     * 1_000 (an 8 KB long[], fits in L1), 100_000 (about 800 KB, fits in L2), and
     * 10_000_000 (about 80 MB long[], bigger than any cache on this machine), to
     * see where the prefetcher stops helping.
     */
    @Param({"1000", "100000", "10000000"})
    public int size;

    private long[] array;
    private Node sequentialHead;
    private Node shuffledHead;
    private List<Long> boxedList;

    @Setup(Level.Trial)
    public void setup() {
        array = PointerChasing.buildArray(size);
        sequentialHead = PointerChasing.buildSequentialLinkedList(size);
        shuffledHead = PointerChasing.buildShuffledLinkedList(size, 42L);
        boxedList = PointerChasing.buildBoxedList(size);
    }

    @Benchmark
    public long array() {
        return PointerChasing.sumArray(array);
    }

    @Benchmark
    public long linkedSequential() {
        return PointerChasing.sumLinked(sequentialHead);
    }

    @Benchmark
    public long linkedShuffled() {
        return PointerChasing.sumLinked(shuffledHead);
    }

    @Benchmark
    public long boxedArrayList() {
        return PointerChasing.sumBoxed(boxedList);
    }
}
