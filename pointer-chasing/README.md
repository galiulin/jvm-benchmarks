# Pointer chasing

Checks the claims from the "Pointer chasing" note with numbers, using JMH
microbenchmarks: it compares four ways to sum N numbers that differ only in memory
access pattern, not algorithm. All four are O(n), one pass, one accumulator.

- `array`: `LongArray`, addresses are predictable (baseline).
- `linkedSequential`: a linked list whose nodes are allocated and linked in the same
  order, so addresses are almost like the array's, just with an extra dereference.
- `linkedShuffled`: the same nodes, but `next` links them in random order. The address
  of the next node no longer correlates with the current one. This is the pointer
  chasing the note is about.
- `boxedArrayList`: `ArrayList<Long>`, also an "array" but boxed. A middle ground
  between `array` and `linkedShuffled`.

## Code

- `src/main/kotlin/io/github/galiulin/benchmarks/pointerchasing/PointerChasing.kt`: the data
  structures (`Node`, array, `ArrayList<Long>`) and the summation functions.
- `src/test/kotlin/io/github/galiulin/benchmarks/pointerchasing/PointerChasingTest.kt`: Kotest,
  checks the builders and sums are correct (not a performance test).
- `src/jmh/java/io/github/galiulin/benchmarks/pointerchasing/PointerChasingBenchmark.java`: the
  benchmark itself (JMH 1.37 via `me.champeau.jmh` 0.7.3). Java, not Kotlin: the
  champeau.jmh plugin runs JMH's annotation processor through javac's APT, and a Java
  source set picks that up without kapt.

## Running it

Correctness tests:

```bash
./gradlew :pointer-chasing:test
```

The benchmark itself (plain JVM path, not `nativeCompile`; takes a few minutes: three
forks per each of the three `size` values):

```bash
./gradlew :pointer-chasing:jmhJar
java -jar pointer-chasing/build/libs/pointer-chasing-0.1.0-jmh.jar \
  -rf json -rff pointer-chasing/build/results/jmh/results.json PointerChasingBenchmark
```

Iteration count, warmup, and fork settings live in `@Warmup`/`@Measurement`/`@Fork`
annotations right on the benchmark class, not in `jmh{}` in `build.gradle.kts`, so they
stay visible next to the code they measure.

## Results

The files in `results/` are from one run (2026-08-26, MacBook Pro, Apple M2 Max, 32 GB,
JDK 25.0.2-open, macOS 26.5.2):

- `results.json`: JMH's machine-readable output (`-rf json`), one object per
  `(benchmark, size)` pair with the full list of 15 measurements. JMH already dropped
  the 5 warmup iterations across 3 forks, so `rawData`/`primaryMetric` hold only the
  measurement iterations.
- `run.log`: the full text log of the same run (warmup, iterations, final summary
  table), as printed to the console.

The analysis lives in the note itself, under "Checking it with numbers (2026-08-26)".

The `size` parameter sweeps 1,000 (an 8 KB `long[]`, fits in L1), 100,000 (about 800 KB,
fits in L2), and 10,000,000 (about 80 MB, bigger than any cache on this machine), to see
where the prefetcher stops helping.
