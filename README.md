# jvm-benchmarks

Microbenchmarks (mostly JVM/JMH), written to back claims in notes and articles with
numbers instead of intuition. Each benchmark is an independent Gradle module in its
own folder: its own `build.gradle.kts`, its own code, its own recorded results. The
modules can be unrelated in subject. The repository just groups them as storage.

Articles link to a specific module subfolder. That folder holds the raw results the
text refers to and a README with the command to reproduce them.

## Benchmarks

| Module | What it measures | Note/article |
|---|---|---|
| [`pointer-chasing/`](pointer-chasing/) | Compares `LongArray`, a linked list (sequential and shuffled), and `ArrayList<Long>`: how the memory access pattern affects summation speed at the same O(n) complexity. | Pointer chasing |

## Requirements

- JDK 25 (tested on `25.0.2-open`)
- The Gradle wrapper is checked in, no separate Gradle install needed

## Layout

```
jvm-benchmarks/
├── build.gradle.kts        # plugin versions, repositories, shared across modules
├── settings.gradle.kts     # include() for each module
├── <benchmark>/
│   ├── build.gradle.kts    # module's own, independent configuration
│   ├── src/main/...        # code under measurement
│   ├── src/test/...        # correctness tests (not performance)
│   ├── src/jmh/...         # the JMH benchmarks themselves
│   ├── results/            # raw run results (results.json, run.log)
│   └── README.md           # what is measured and how to reproduce it
```

Adding a new benchmark is a new folder at this level plus an
`include("<name>")` line in `settings.gradle.kts`.

## License

MIT, see [LICENSE](LICENSE).
