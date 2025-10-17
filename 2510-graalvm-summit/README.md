# Native Benchmarking with JMH

## Source Code

Base `CharAt` benchmark:
```java
public class CharAt
{
    private String strLatin1;
    private int charAtIndex;

    @Setup
    public void setup()
    {
        strLatin1 = "Latin1 string";
        charAtIndex = 3;
    }

    @Benchmark
    public char latin1()
    {
        return strLatin1.charAt(charAtIndex);
    }
}
```

`String.charAt` implementation:
```java
class String
{
    static final byte LATIN1 = 0;
    final byte[] value;
    final byte coder;

    char charAt(int index)
    {
        if (isLatin1())
        {
            return StringLatin1.charAt(value, index);
        }
        // ...
    }

    boolean isLatin1()
    {
        return /* ... */ && coder == LATIN1;
    }
}
```

`StringLatin1.charAt` implementation:
```java
class StringLatin1
{
    static char charAt(byte[] value, int index)
    {
        checkIndex(index, value.length);
        return (char)(value[index] & 0xff);
    }
}
```

The `checkIndex` implementation delegates to `Preconditions.checkIndex` whose implementation is:

```java
@IntrinsicCandidate
public static <X extends RuntimeException>
int checkIndex(int index, int length,
               BiFunction<String, List<Number>, X> oobef) {
    if (index < 0 || index >= length)
        throw outOfBoundsCheckIndex(oobef, index, length);
    return index;
}
```

Generated JMH source code:
```java
class CharAt_latin1_jmhTest
{
    public static void latin1_avgt_jmhStub(
        InfraControl control
        , RawResults result
        , Blackhole blackhole
        , CharAt_jmhType l_charat0_0
    ) throws Throwable
    {
        long operations = 0;
        long realTime = 0;
        result.startTime = System.nanoTime();
        do
        {
            blackhole.consume(l_charat0_0.latin1());
            operations++;
        }
        while(!control.isDone);
        result.stopTime = System.nanoTime();
        result.realTime = realTime;
        result.measuredOps = operations;
    }
}
```

## Ahead-of-time Benchmarking

With GraalVM CE 25:

```bash
# JMH version: fibula:999-SNAPSHOT
# VM version: JDK 25, Substrate VM, GraalVM CE 25+37.1
...
Benchmark      Mode  Cnt  Score   Error  Units
CharAt.latin1  avgt    5  2.981 ± 0.006  ns/op
```

With Oracle GraalVM 25:

```bash
# JMH version: fibula:999-SNAPSHOT
# VM version: JDK 25, Substrate VM, Oracle GraalVM 25+37.1
...
Benchmark      Mode  Cnt  Score    Error  Units
CharAt.latin1  avgt    5  0.813 ±  0.001  ns/op
```

That's roughly a 3.6x difference in performance.
What makes Oracle GraalVM faster?

### Structs

There are differences in the structs that GraalVM CE and Oracle GraalVM use.
Although these differences do not explain the performance difference,
they will be useful guide for further analysis.
Below can be found the struct definitions for `org.sample.strings.CharAt` and `java.lang.String` classes.

GraalVM CE:

```bash
/* offset      |    size */  type = class org.sample.strings.CharAt : public java.lang.Object {
/*      8      |       8 */    class _z_.java.lang.String *strLatin1;
/*     16      |       4 */    int charAtIndex;
/* XXX  4-byte padding   */
                               /* total size (bytes):   24 */

/* offset      |    size */  type = class java.lang.String : public java.lang.Object {
/*      8      |       8 */    class _z_.byte[] *value;
/*     16      |       4 */    int hash;
/*     20      |       1 */    byte coder;
/*     21      |       1 */    boolean hashIsZero;
                               static class _z_.java.io.ObjectStreamField[] *serialPersistentFields;
                             public:
                               static class _z_.java.util.Comparator *CASE_INSENSITIVE_ORDER;
                             private:
                               static long serialVersionUID;
                               static char REPL;
                             public:
                               static byte LATIN1;
                               static byte UTF16;
                               static boolean COMPACT_STRINGS;
/* XXX 10-byte padding   */
                               /* total size (bytes):   32 */
                             }

/* offset      |    size */  type = class byte [] : public java.lang.Object {
                             public:
/*     12      |       4 */    int len;
/*     16      |       0 */    byte data[0];

                               /* total size (bytes):   16 */
                             }
```

Oracle GraalVM:

```bash
/* offset      |    size */  type = class org.sample.strings.CharAt : public java.lang.Object {
/*      4      |       4 */    class _z_.java.lang.String *strLatin1;
/*      8      |       4 */    int charAtIndex;
/* XXX  4-byte padding   */

                               /* total size (bytes):   16 */
                             }
/* offset      |    size */  type = class java.lang.String : public java.lang.Object {
/*      4      |       4 */    class _z_.byte[] *value;
/*      8      |       4 */    int hash;
/*     12      |       1 */    byte coder;
/*     13      |       1 */    boolean hashIsZero;
                               static class _z_.java.io.ObjectStreamField[] *serialPersistentFields;
                             public:
                               static class _z_.java.util.Comparator *CASE_INSENSITIVE_ORDER;
                             private:
                               static long serialVersionUID;
                               static char REPL;
                             public:
                               static byte LATIN1;
                               static byte UTF16;
                               static boolean COMPACT_STRINGS;
/* XXX  2-byte padding   */

                               /* total size (bytes):   16 */
                             }

/* offset      |    size */  type = class byte [] : public java.lang.Object {
                             public:
/*      4      |       4 */    int len;
/*      8      |       0 */    byte data[0];

                               /* total size (bytes):    8 */
                             }
```

### Inlining

The `perf annotate` output for Oracle GraalVM shows that the call chain from `CharAt.latin1` all the way down to `StringLatin1.charAt` has been inlined:

```bash
    0xc1c3e0 <void org.sample.strings.jmh_generated.CharAt_latin1_jmhTest::latin1_avgt_jmhStub(org.openjdk.jmh.runner.InfraControl*, org.openjdk.jmh.results.RawResults*, org.openjdk.jmh.infra.BenchmarkParams*, org.openjdk.jmh.infra.IterationParams*, org.openjdk.jmh.infra.ThreadParams*, org.openjdk.jmh.infra.Blackhole*, org.openjdk.jmh.infra.Control*, org.sample.strings.jmh_generated.CharAt_jmhType*)>:
e0:   nop
      leaq       0x69e038(%r14),%rdx    ;; store byte[] backing String at fixed address into rdx (r14="Latin1 string")
      movl       0x8(%rcx),%ebx         ;; store index into ebx (rcx=CharAt, 0x8(%rcx)=CharAt.charAtIndex)
      movzbl     0x8(%rdx,%rbx),%ebx    ;; computes `value[index] & 0xff` (rdx=byte[] value, rbx=int index)
      incq       %rax                   ;; operations++
    ↑ jg         e0
```

On the other hand, the `perf annotate` output for GraalVM CE demonstrates limited inlining:

```bash
     0xcc1b60 <void org.sample.strings.jmh_generated.CharAt_latin1_jmhTest::latin1_avgt_jmhStub(org.openjdk.jmh.runner.InfraControl*, org.openjdk.jmh.results.RawResults*, org.openjdk.jmh.infra.BenchmarkParams*, org.openjdk.jmh.infra.IterationParams*, org.openjdk.jmh.infra.ThreadParams*, org.openjdk.jmh.infra.Blackhole*, org.openjdk.jmh.infra.Control*, org.sample.strings.jmh_generated.CharAt_jmhType*)>:
120:   movl       0x10(%rax),%esi
       movq       %rdx,%rdi
     → callq      char java.lang.String::charAt(int)
       incq       %rax                                ;; operations++
     ↑ jne        120

     0x714960 <char java.lang.String::charAt(int)>:
     → callq  char java.lang.StringLatin1::charAt(byte[]*, int)

     0x72c480 <char java.lang.StringLatin1::charAt(byte[]*, int)>:
       movzbl  0x10(%rdi,%rax),%edx                    ;; computes `value[index] & 0xff` (rdi=byte[] value, rax=int index)
```

A fairer comparison between Oracle GraalVM and GraalVM CE can be obtained by using the JMH `@CompilerControl(DONT_INLINE)` directive to stop the benchmarked method being inlined into the JMH generated code:

```java
public class DontInlineCharAt
{
    // ...

    @Benchmark
    @CompilerControl(DONT_INLINE)
    public char latin1()
    {
        return strLatin1.charAt(charAtIndex);
    }
}
```

If we do that, we obtain these results:

```bash
Benchmark                Mode  Cnt  Score   Error  Units
DontInlineCharAt.latin1  avgt    5  4.334 ± 0.004  ns/op # GraalVM CE 25
DontInlineCharAt.latin1  avgt    5  2.182 ± 0.022  ns/op # Oracle GraalVM 25
```

The difference is now ~2x.
In Oracle GraalVM we can verify that the top level inlining into the JMH generated code didn't happen,
but further down the aggressive inlining happens:

```bash
    0xc1f460 <void org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub(org.openjdk.jmh.runner.InfraControl*, org.openjdk.jmh.results.RawResults*, org.openjdk.jmh.infra.BenchmarkParams*, org.openjdk.jmh.infra.IterationParams*, org.openjdk.jmh.infra.ThreadParams*, org.openjdk.jmh.infra.Blackhole*, org.openjdk.jmh.infra.Control*, org.sample.strings.jmh_generated.DontInlineCharAt_jmhType*)>:
a0:   movq       0x10(%rsp),%rax
      movq       %rax,%rdi
    → callq      char org.sample.strings.DontInlineCharAt::latin1()
      incq       %rax                                                  ;; operations++
    ↑ jmp        a0

    0xc19e60 <char org.sample.strings.DontInlineCharAt::latin1()>:
      movzbl 0x8(%rdi,%rax),%eax    ;; computes `value[index] & 0xff` (rdi=byte[] value, rax=int index)
```

### Bound Check

As per `StringLatin1.charAt` java source code implementation,
the `byte[]` length should be checked against the index,
but instead Oracle GraalVM code is checking the index against the hardcoded value of the String's `byte[]` length of `13`:

```bash
    0xc19e60 <char org.sample.strings.DontInlineCharAt::latin1()>:
      movl   0x8(%rdi),%eax         ;; store index into eax (rdi=DontInlineCharAt, 0x8(%rdi)=DontInlineCharAt.charAtIndex)
      leaq   0x69ca88(%r14),%rdi    ;; store byte[] backing String at fixed address into rdi (r14="Latin1 string")
      cmpl   $0xd,%eax              ;; index >= 13? "Latin1 string" is 12 chars long
    ↓ jae    68                     ;; jump to 68 and throw RuntimeException for out of bounds
      movzbl 0x8(%rdi,%rax),%eax    ;; computes `value[index] & 0xff` (rdi=byte[] value, rax=int index)
68:   nop
      movq   %rsi,%rdi
      movl   %eax,%esi
      movl   $0xd,%edx
      movl   %eax,0x14(%rsp)
    → callq  java.lang.RuntimeException* jdk.internal.util.Preconditions::outOfBoundsCheckIndex(java.util.function.BiFunction*, int, int)
```

In GraalVM CE, we observe that the `len` field is extracted from the `byte[]` (in position `12` (`0xc`) as per the struct for `byte[]`),
and if the index parameter is bigger or equals than the length it jumps to section that produces an out of bounds runtime exception:

```bash
char java.lang.StringLatin1::charAt(byte[]*, int)() /home/g/src/fibula-show/2510-graalvm-summit/target/benchmarks
Percent      0x72c480 <char java.lang.StringLatin1::charAt(byte[]*, int)>:
      movl    0xc(%rdi),%edx         ;; edx = value.length (rdi=byte[] value, 0xc=len position in byte[] struct)
      cmpl    %esi,%edx              ;; index >= value.length? (edx=value.length, esi=index)
    ↓ jbe     59
      movzbl  0x10(%rdi,%rax),%edx
59:   movq    %rdi,0x8(%rsp)
      nop
      movq    %rcx,%rdi
      movl    %esi,%ecx
      movl    %ecx,0x14(%rsp)
    → callq   java.lang.RuntimeException* jdk.internal.util.Preconditions::outOfBoundsCheckIndex(java.util.function.BiFunction*, int, int)
```

### Latin1 Check

In the GraalVM CE assembly we can observe how the `String.coder` field is checked to see if the String is latin1 or not:

```bash
Percent      0x714960 <char java.lang.String::charAt(int)>:
      cmpb   $0x0,0x14(%rdi)    ;; coder == 0? (rdi=String, 0x14=coder position in String struct)
    ↓ je     5b                 ;; jump to 5b if coder == 0 (isLatin1 == true)
      nop
      nop
      nop
      movl   %esi,%edi
      movl   %esi,0x14(%rsp)
      movq   %rax,%rsi
      movq   %rax,0x8(%rsp)
    → callq  void java.lang.StringUTF16::checkIndex(int, byte[]*)
5b:   movq   %rax,%rcx
      movl   %esi,0x14(%rsp)
      nop
      movq   %rcx,%rdi
    → callq  char java.lang.StringLatin1::charAt(byte[]*, int)
```

No such check can be found in the Oracle GraalVM assembly code.

## PGO

What numbers do we obtain if we use PGO with `DontInlineCharAt`?

```bash
# JMH version: fibula:999-SNAPSHOT
# VM version: JDK 25, Substrate VM, Oracle GraalVM 25+37.1
# *** WARNING: This VM is not supported by JMH. The produced benchmark data can be completely wrong.
# VM invoker: target/benchmarks.output/default/benchmarks
...
# Benchmark: org.sample.strings.DontInlineCharAt.latin1

# Run progress: 0.00% complete, ETA 00:00:20
# Warmup Fork: 1 of 1
# Warmup Iteration   1: 4.361 ns/op
# Warmup Iteration   2: 4.361 ns/op
# Warmup Iteration   3: 4.361 ns/op
# Warmup Iteration   4: 4.361 ns/op
# Warmup Iteration   5: 4.357 ns/op
# Warmup Iteration   6: 4.361 ns/op
# Warmup Iteration   7: 4.358 ns/op
# Warmup Iteration   8: 4.371 ns/op
# Warmup Iteration   9: 4.359 ns/op
# Warmup Iteration  10: 4.359 ns/op
Iteration   1: 4.362 ns/op
Iteration   2: 4.359 ns/op
Iteration   3: 4.360 ns/op
Iteration   4: 4.358 ns/op
Iteration   5: 4.358 ns/op

# PGO: Instrumented warmup fork complete
# PGO: Rebuild bundle with profiling data
# PGO: Rebuild native from bundle

# Run progress: 50.00% complete, ETA 00:00:10
# Fork: 1 of 1
# Warmup Iteration   1: 1.607 ns/op
# Warmup Iteration   2: 1.597 ns/op
# Warmup Iteration   3: 1.624 ns/op
# Warmup Iteration   4: 1.599 ns/op
# Warmup Iteration   5: 1.595 ns/op
# Warmup Iteration   6: 1.605 ns/op
# Warmup Iteration   7: 1.598 ns/op
# Warmup Iteration   8: 1.599 ns/op
# Warmup Iteration   9: 1.602 ns/op
# Warmup Iteration  10: 1.600 ns/op
Iteration   1: 1.597 ns/op
Iteration   2: 1.600 ns/op
Iteration   3: 1.601 ns/op
Iteration   4: 1.602 ns/op
Iteration   5: 1.602 ns/op


Result "org.sample.strings.DontInlineCharAt.latin1":
  1.600 ±(99.9%) 0.009 ns/op [Average]
  (min, avg, max) = (1.597, 1.600, 1.602), stdev = 0.002
  CI (99.9%): [1.591, 1.610] (assumes normal distribution)


# Run complete. Total time: 00:00:45

Benchmark                Mode  Cnt  Score   Error  Units
DontInlineCharAt.latin1  avgt    5  1.600 ± 0.009  ns/op
```

How does PGO achieve this improvement?
The inlining in `DontInlineCharAt::latin1` looks the same,
but PGO has discovered that `latin1` is called a lot within a loop,
so it decided to unroll the calls.
One clear benefit of unrolling is fewer branches being taken.

```bash
     0x480ae0 <org.openjdk.jmh.results.BenchmarkTaskResult* org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_AverageTime%%H7(org.openjdk.jmh.runner.InfraControl*, org.openjdk.jmh.infra.ThreadParams*)>:
2a0:   movq       %rax,0xb0(%rsp)
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #1
       leaq       0x2(%rax),%rcx                                        ;; rcx = operations(rax) + 2
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        b56
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #2
       leaq       0x3(%rax),%rcx                                        ;; rcx = operations(rax) + 3
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        b7b
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #3
       leaq       0x4(%rax),%rcx                                        ;; rcx operations(= r)ax + 4
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        ba0
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #4
       leaq       0x5(%rax),%rcx                                        ;; rcx = operations(rax) + 5
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        bc5
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #5
       leaq       0x6(%rax),%rcx                                        ;; rcx = operations(rax) + 6
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        bea
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #6
       leaq       0x7(%rax),%rcx                                        ;; rcx = operations(rax) + 7
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        c13
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #7
       leaq       0x8(%rax),%rcx                                        ;; rcx = operations(rax) + 8
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        c38
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #8
       leaq       0x9(%rax),%rcx                                        ;; rcx = operations(rax) + 9
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        c57
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #9
       leaq       0xa(%rax),%rcx                                        ;; rcx = operations(rax) + 10
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        c76
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #10
       leaq       0xb(%rax),%rcx                                        ;; rcx = operations(rax) + 11
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        c95
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #11
       leaq       0xc(%rax),%rcx                                        ;; rcx = operations(rax) + 12
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        cb4
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #12
       leaq       0xd(%rax),%rcx                                        ;; rcx = operations(rax) + 13
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        cd3
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #13
       leaq       0xe(%rax),%rcx                                        ;; rcx = operations(rax) + 14
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        cf2
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #14
       leaq       0xf(%rax),%rcx                                        ;; rcx = operations(rax) + 15
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        d11
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #15
       leaq       0x10(%rax),%rax                                       ;; operations(rax) += 16
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↓ jne        d30
     → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #16
       incq       %rax                                                  ;; operations(rax)++
       cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
     ↑ je         2a0
```

```bash
0x480a40 <char org.sample.strings.DontInlineCharAt::latin1()>:
  movl   0x8(%rdi),%eax         ;; store index into eax (rdi=DontInlineCharAt, 0x8(%rdi)=DontInlineCharAt.charAtIndex)
  leaq   0x6d1e80(%r14),%rdi    ;; store byte[] backing String at fixed address into rdi (r14="Latin1 string")
  movzbl 0x8(%rdi,%rax),%eax    ;; computes `value[index] & 0xff` (rdi=byte[] value, rax=int index)
```

## HotSpot

What about HotSpot? How does it perform?

```bash
# JMH version: fibula:999-SNAPSHOT
# VM version: JDK 25, OpenJDK 64-Bit Server VM, 25+36-LTS
...
# Benchmark: org.sample.strings.DontInlineCharAt.latin1

Benchmark                Mode  Cnt  Score   Error  Units
DontInlineCharAt.latin1  avgt    5  4.064 ± 0.003  ns/op # HotSpot JIT
```

For comparison:

```bash
Benchmark                Mode  Cnt  Score   Error  Units
DontInlineCharAt.latin1  avgt    5  4.334 ± 0.004  ns/op # GraalVM CE 25
DontInlineCharAt.latin1  avgt    5  4.064 ± 0.003  ns/op # HotSpot JIT
DontInlineCharAt.latin1  avgt    5  2.182 ± 0.022  ns/op # Oracle GraalVM 25
DontInlineCharAt.latin1  avgt    5  1.600 ± 0.009  ns/op # Oracle GraalVM 25 PGO
```

A HotSpot run with `perfasm` gives us some clues on how things differ.
Compared with PGO, there's no unrolling of the loop in HotSpot.
HotSpot also has additional assembly to deal with safepoints and deoptimizations.
Per iteration, more instructions needed to be executed and lack of unrolling means there's no amortization of some of those costs.

```bash
Compiled method (c2) 1952 1044       4       org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub (57 bytes)
↗  90:   mov    0x38(%rsp),%rsi              ; load the DontInlineCharAt obj instance
|        call   0x00007fe37052eae0           ; invoke obj.latin1()
|        nopl   0x1000194(%rax,%rax,1)       ; post call nop (for deoptimization?)
|        mov    (%rsp),%r10
|        movzbl 0x120(%r10),%r10d            ; load isDone into r10d
|        mov    0x30(%r15),%r11              ; load safepoint polling page pointer to r11
|        inc    %rbp                         ; operations++
|        test   %eax,(%r11)                  ; safepoint poll
|        test   %r10d,%r10d                  ; isDone?
╰        je     90
```

Similar to non-PGO Oracle GraalVM 25, it has inlined calls all the way, but there are considerable differences.
HotSpot doesn't just compare the index with the constant `13`.
Instead, it queries the array length of the `byte[]` and compares that with the index:
Also, we observe that the coder field is checked to see if the `String` is latin1 or not,
but this check does not appear in either the PGO or the non-PGO Oracle GraalVM runs:

```bash
Compiled method (c2) 953 1008       4       org.sample.strings.DontInlineCharAt::latin1 (12 bytes)
  mov    0x10(%rsi),%r10d             ; load strLatin1 String field into r10
  movsbl 0x10(%r12,%r10,8),%r9d       ; extract strLatin1.coder byte into r9
  test   %r9d,%r9d                    ; coder != 0?
  jne    0x00007fe370c2db50           ; jump to deal with utf16 string (not taken)
  mov    0x14(%r12,%r10,8),%r10d      ; extract strLatin1.value byte[] to r10
  mov    0xc(%r12,%r10,8),%ebp        ; extract byte[] length to ebp
  cmp    %ebp,%r11d                   ; index >= length? (ebp=byte[] length, r11=int index)
  jae    0x00007fe370c2daef           ; jump to deal with out of bounds
  shl    $0x3,%r10
  movzbl 0x10(%r10,%r11,1),%eax       ; computes `value[index] & 0xff` (r10=byte[] value, r11=int index)
```
