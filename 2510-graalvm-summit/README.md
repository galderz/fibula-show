# TBD

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

In Oracle GraalVM `CharAt` gets mapped to a struct like this:

```bash
(gdb) ptype /o 'org.sample.strings.CharAt'
/* offset      |    size */  type = class org.sample.strings.CharAt : public java.lang.Object {
/*      4      |       4 */    class _z_.java.lang.String *strLatin1;
/*      8      |       4 */    int charAtIndex;
/* XXX  4-byte padding   */

                               /* total size (bytes):   16 */
                             }
```

The `perf annotate` output for Oracle GraalVM shows that the call chain from `CharAt.latin1` all the way down to `StringLatin1.charAt` has been inlined:

```bash
Percent       0xc1c3e0 <void org.sample.strings.jmh_generated.CharAt_latin1_jmhTest::latin1_avgt_jmhStub(org.openjdk.jmh.runner.InfraControl*, org.openjdk.jmh.results.RawResults*, org.openjdk.jmh.infra.BenchmarkParams*, org.openjdk.jmh.infra.IterationParams*, org.openjdk.jmh.infra.ThreadParams*, org.openjdk.jmh.infra.Blackhole*, org.openjdk.jmh.infra.Control*, org.sample.strings.jmh_generated.CharAt_jmhType*)>:
  11.90   e0:   nop             
                leaq       0x69e038(%r14),%rdx    ;; store byte[] backing String at fixed address into rdx (r14="Latin1 string")
                movl       0x8(%rcx),%ebx         ;; store index into ebx (rcx=CharAt, 0x8(%rcx)=CharAt.charAtIndex)
                movzbl     0x8(%rdx,%rbx),%ebx    ;; computes `value[index] & 0xff` (rdx=byte[] value, rbx=int index)
                incq       %rax                   ;; operations++ 
   0.19       ↑ jg         e0   
```

On the other hand, the `perf annotate` output for GraalVM CE demonstrates limited inlining:

```bash
Percent       0xcc1b60 <void org.sample.strings.jmh_generated.CharAt_latin1_jmhTest::latin1_avgt_jmhStub(org.openjdk.jmh.runner.InfraControl*, org.openjdk.jmh.results.RawResults*, org.openjdk.jmh.infra.BenchmarkParams*, org.openjdk.jmh.infra.IterationParams*, org.openjdk.jmh.infra.ThreadParams*, org.openjdk.jmh.infra.Blackhole*, org.openjdk.jmh.infra.Control*, org.sample.strings.jmh_generated.CharAt_jmhType*)>:
         120:   movl       0x10(%rax),%esi
                movq       %rdx,%rdi
              → callq      char java.lang.String::charAt(int)
   0.54         incq       %rax                                ;; operations++ 
  13.01       ↑ jne        120

Percent      0x714960 <char java.lang.String::charAt(int)>:
   0.15      → callq  char java.lang.StringLatin1::charAt(byte[]*, int)

Percent      0x72c480 <char java.lang.StringLatin1::charAt(byte[]*, int)>:
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
but further down the aggressive inlining happens.
But, some debatable things are happening as well.
For example, as per `StringLatin1.charAt` implementation,
the byte[] array length should be checked against the index,
but instead the code is checking the index against the hardcoded value of the String's byte[] length of 13:

```bash
Percent       0xc1f460 <void org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub(org.openjdk.jmh.runner.InfraControl*, org.openjdk.jmh.results.RawResults*, org.openjdk.jmh.infra.BenchmarkParams*, org.openjdk.jmh.infra.IterationParams*, org.openjdk.jmh.infra.ThreadParams*, org.openjdk.jmh.infra.Blackhole*, org.openjdk.jmh.infra.Control*, org.sample.strings.jmh_generated.DontInlineCharAt_jmhType*)>:
          a0:   movq       0x10(%rsp),%rax
                movq       %rax,%rdi
              → callq      char org.sample.strings.DontInlineCharAt::latin1()    
  18.25         incq       %rax                                                  ;; operations++
              ↑ jmp        a0   

Percent      0xc19e60 <char org.sample.strings.DontInlineCharAt::latin1()>:
               movl   0x8(%rdi),%eax         ;; store index into eax (rdi=DontInlineCharAt, 0x8(%rdi)=DontInlineCharAt.charAtIndex)
               leaq   0x69ca88(%r14),%rdi    ;; store byte[] backing String at fixed address into rdi (r14="Latin1 string")
  17.15        cmpl   $0xd,%eax              ;; index >= 13? "Latin1 string" is 12 chars long
             ↓ jae    68                     ;; jump to 68 and throw RuntimeException for out of bounds 
  16.66        movzbl 0x8(%rdi,%rax),%eax    ;; computes `value[index] & 0xff` (rdi=byte[] value, rax=int index)
         68:   nop            
               movq   %rsi,%rdi
               movl   %eax,%esi
               movl   $0xd,%edx
               movl   %eax,0x14(%rsp)
             → callq  java.lang.RuntimeException* jdk.internal.util.Preconditions::outOfBoundsCheckIndex(java.util.function.BiFunction*, int, int)
```

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
One clear benefit of unrolling is less instruction jumps.

```bash
Percent        0x480ae0 <org.openjdk.jmh.results.BenchmarkTaskResult* org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_AverageTime%%H7(org.openjdk.jmh.runner.InfraControl*, org.openjdk.jmh.infra.ThreadParams*)>:
   0.89   2a0:   movq       %rax,0xb0(%rsp)
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #1
                 leaq       0x2(%rax),%rcx                                        ;; rcx = operations(rax) + 2 
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   2.29        ↓ jne        b56   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #2
                 leaq       0x3(%rax),%rcx                                        ;; rcx = operations(rax) + 3
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   2.25        ↓ jne        b7b   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #3
                 leaq       0x4(%rax),%rcx                                        ;; rcx operations(= r)ax + 4
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   2.41        ↓ jne        ba0   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #4
                 leaq       0x5(%rax),%rcx                                        ;; rcx = operations(rax) + 5
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
               ↓ jne        bc5   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #5
                 leaq       0x6(%rax),%rcx                                        ;; rcx = operations(rax) + 6
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   2.02        ↓ jne        bea   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #6
                 leaq       0x7(%rax),%rcx                                        ;; rcx = operations(rax) + 7
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   2.43        ↓ jne        c13   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #7
                 leaq       0x8(%rax),%rcx                                        ;; rcx = operations(rax) + 8
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   2.10        ↓ jne        c38   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #8
                 leaq       0x9(%rax),%rcx                                        ;; rcx = operations(rax) + 9
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   3.38        ↓ jne        c57   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #9
                 leaq       0xa(%rax),%rcx                                        ;; rcx = operations(rax) + 10
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
               ↓ jne        c76   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #10
                 leaq       0xb(%rax),%rcx                                        ;; rcx = operations(rax) + 11
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   3.20        ↓ jne        c95   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #11
                 leaq       0xc(%rax),%rcx                                        ;; rcx = operations(rax) + 12
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   2.31        ↓ jne        cb4   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #12
   0.97          leaq       0xd(%rax),%rcx                                        ;; rcx = operations(rax) + 13
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   4.35        ↓ jne        cd3   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #13
                 leaq       0xe(%rax),%rcx                                        ;; rcx = operations(rax) + 14
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
               ↓ jne        cf2   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #14
                 leaq       0xf(%rax),%rcx                                        ;; rcx = operations(rax) + 15
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   3.12        ↓ jne        d11   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #15
                 leaq       0x10(%rax),%rax                                       ;; operations(rax) += 16
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   3.27        ↓ jne        d30   
               → callq      char org.sample.strings.DontInlineCharAt::latin1()    ;; unrolled call #16
   1.01          incq       %rax                                                  ;; operations(rax)++  
                 cmpb       $0x0,0x13c(%rsi)                                      ;; isDone?
   4.59        ↑ je         2a0      

Percent      0x480a40 <char org.sample.strings.DontInlineCharAt::latin1()>:
               movl   0x8(%rdi),%eax         ;; store index into eax (rdi=DontInlineCharAt, 0x8(%rdi)=DontInlineCharAt.charAtIndex)
   0.02        leaq   0x6d1e80(%r14),%rdi    ;; store byte[] backing String at fixed address into rdi (r14="Latin1 string")
   0.02        movzbl 0x8(%rdi,%rax),%eax    ;; computes `value[index] & 0xff` (rdi=byte[] value, rax=int index)
```

To finish up, how does HotSpot JIT compare with PGO?

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
Compared with PGO, there's no unrolling of the loop in HotSpot:

```bash
....[Hottest Region 1]..............................................................................
C2, level 4, org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub, version 6, compile id 747 

              0x00007f683c0135f9:   movzbl 0x120(%r10),%r11d            ; implicit exception: dispatches to 0x00007f683c0136d0
                                                                        ;*getfield isDone {reexecute=0 rethrow=0 return_oop=0}
                                                                        ; - org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub@30 (line 167)
              0x00007f683c013601:   mov    $0x1,%ebp
              0x00007f683c013606:   test   %r11d,%r11d
          ╭   0x00007f683c013609:   jne    0x00007f683c01363f           ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
          │                                                             ; - org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub@33 (line 167)
          │   0x00007f683c01360b:   nopl   0x0(%rax,%rax,1)             ;*aload_1 {reexecute=0 rethrow=0 return_oop=0}
          │                                                             ; - org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub@36 (line 168)
          │↗  0x00007f683c013610:   mov    0x38(%rsp),%rsi
   6.50%  ││  0x00007f683c013615:   xchg   %ax,%ax
          ││  0x00007f683c013617:   call   0x00007f683b8656e0           ; ImmutableOopMap {[0]=Oop [8]=Oop [48]=Oop [56]=Oop }
          ││                                                            ;*invokevirtual latin1 {reexecute=0 rethrow=0 return_oop=0}
          ││                                                            ; - org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub@17 (line 165)
          ││                                                            ;   {optimized virtual_call}
  70.74%  ││  0x00007f683c01361c:   nopl   0x1000194(%rax,%rax,1)       ;*invokevirtual latin1 {reexecute=0 rethrow=0 return_oop=0}
          ││                                                            ; - org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub@17 (line 165)
          ││                                                            ;   {post_call_nop}
          ││  0x00007f683c013624:   mov    (%rsp),%r10
          ││  0x00007f683c013628:   movzbl 0x120(%r10),%r10d            ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
          ││                                                            ; - org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub@33 (line 167)
   0.02%  ││  0x00007f683c013630:   mov    0x30(%r15),%r11
          ││  0x00007f683c013634:   inc    %rbp                         ; ImmutableOopMap {[0]=Oop [8]=Oop [48]=Oop [56]=Oop }
          ││                                                            ;*ifeq {reexecute=1 rethrow=0 return_oop=0}
          ││                                                            ; - (reexecute) org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub@33 (line 167)
          ││  0x00007f683c013637:   test   %eax,(%r11)                  ;   {poll}
          ││  0x00007f683c01363a:   test   %r10d,%r10d
          │╰  0x00007f683c01363d:   je     0x00007f683c013610           ;*aload_1 {reexecute=0 rethrow=0 return_oop=0}
          │                                                             ; - org.sample.strings.jmh_generated.DontInlineCharAt_latin1_jmhTest::latin1_avgt_jmhStub@36 (line 168)
          ↘   0x00007f683c01363f:   movabs $0x7f685187d9f0,%r10         ;   {runtime_call os::javaTimeNanos()}
              0x00007f683c013649:   call   *%r10
```

Similar to non-PGO Oracle GraalVM 25, it has inlined calls all the way, but there are considerable differences.
HotSpot doesn't just compare the... 

```bash
....[Hottest Region 2]..............................................................................
C2, level 4, org.sample.strings.DontInlineCharAt::latin1, version 2, compile id 711 

                                                                       ; - java.lang.String::charAt@1 (line 1623)
                                                                       ; - org.sample.strings.DontInlineCharAt::latin1@8 (line 41)
             0x00007f683c010f3d:   mov    0x14(%r12,%r10,8),%r10d      ;*getfield value {reexecute=0 rethrow=0 return_oop=0}
                                                                       ; - java.lang.String::charAt@8 (line 1624)
                                                                       ; - org.sample.strings.DontInlineCharAt::latin1@8 (line 41)
             0x00007f683c010f42:   mov    0xc(%r12,%r10,8),%ebp        ; implicit exception: dispatches to 0x00007f683c010fdc
                                                                       ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                                       ; - java.lang.StringLatin1::charAt@2 (line 45)
                                                                       ; - java.lang.String::charAt@12 (line 1624)
                                                                       ; - org.sample.strings.DontInlineCharAt::latin1@8 (line 41)
   6.83%     0x00007f683c010f47:   cmp    %ebp,%r11d
          ╭  0x00007f683c010f4a:   jae    0x00007f683c010f6b           ;*invokestatic checkIndex {reexecute=0 rethrow=0 return_oop=0}
          │                                                            ; - java.lang.String::checkIndex@5 (line 4904)
          │                                                            ; - java.lang.StringLatin1::charAt@3 (line 45)
          │                                                            ; - java.lang.String::charAt@12 (line 1624)
          │                                                            ; - org.sample.strings.DontInlineCharAt::latin1@8 (line 41)
          │  0x00007f683c010f4c:   cmp    %ebp,%r11d
          │  0x00007f683c010f4f:   jae    0x00007f683c010f88
          │  0x00007f683c010f51:   shl    $0x3,%r10
          │  0x00007f683c010f55:   movzbl 0x10(%r10,%r11,1),%eax       ;*iand {reexecute=0 rethrow=0 return_oop=0}
          │                                                            ; - java.lang.StringLatin1::charAt@12 (line 46)
          │                                                            ; - java.lang.String::charAt@12 (line 1624)
          │                                                            ; - org.sample.strings.DontInlineCharAt::latin1@8 (line 41)
          │  0x00007f683c010f5b:   add    $0x20,%rsp
          │  0x00007f683c010f5f:   pop    %rbp
          │  0x00007f683c010f60:   cmp    0x28(%r15),%rsp              ;   {poll_return}
   6.20%  │  0x00007f683c010f64:   ja     0x00007f683c010ff0
          │  0x00007f683c010f6a:   ret
          ↘  0x00007f683c010f6b:   mov    $0xffffffe4,%esi
             0x00007f683c010f70:   mov    %r11d,0x4(%rsp)
             0x00007f683c010f75:   mov    %r10d,0x8(%rsp)
             0x00007f683c010f7a:   nop
             0x00007f683c010f7b:   call   0x00007f683b937fe0           ; ImmutableOopMap {[8]=NarrowOop }
                                                                       ;*invokestatic checkIndex {reexecute=0 rethrow=0 return_oop=0}
                                                                       ; - java.lang.String::checkIndex@5 (line 4904)
                                                                       ; - java.lang.StringLatin1::charAt@3 (line 45)
                                                                       ; - java.lang.String::charAt@12 (line 1624)
....................................................................................................
  13.03%  <total for region 2>
```
