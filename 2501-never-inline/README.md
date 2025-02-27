# Never Inline

## Get native results

Build:

```shell
mvn package
```

Then just run:
```shell
java -jar target/benchmarks.jar
```

You can try trace inlining information (fails with GraalVM for DK 21 so use say GraalVM for JDK 23),
but that won't show whether inline annotations have been applied or not.

```shell
mvn package -DbuildArgs=-H:+TraceInlining
```

To observe `@CompilerControl(DONT_INLINE)`,
built native image with PGO perf profile:

```shell
mvn package -Dpgo.perf
```

Then run with the DWARF profiler:

```shell
java -jar target/benchmarks.jar -prof org.mendrugo.fibula.PerfDwarfProfiler:events=cycles:pp -f 1 charAtLatin1
```

Finally inspect the perf data with `perf annotate`:

```shell
perf annotate -i org.sample.strings.CharAt.charAtLatin1-AverageTime.perfbin
```

And you will observe:

```asm
Samples: 10K of event 'cycles:pp', 1000 Hz, Event count (approx.): 36831957154
org.openjdk.jmh.results.BenchmarkTaskResult* org.sample.strings.jmh_generated.CharAt_charAtLatin1_jmhTest::charAtLatin1_AverageTime%%Hot_Method_Key_6(org.openjdk.jmh.runner.InfraControl*, org.openjdk.jmh.infra.ThreadParams*)  /home/g/1/fibula-show/2501-never-inline/target/benchmarks-optimized.output/default/benchma
Percent│
       │
...
       │ 412:   mov         0x14(%rdi),%eax
       │        test        %eax,%eax
       │      ↓ je          1270
       │        movb        $0x1,0x88(%r14,%rax,8)
       │        lock        addl        $0x0,(%rsp)
       │        mov         0x10(%rdi),%eax
       │        lea         (%r14,%rax,8),%rcx
       │        mov         %rcx,0x18(%rsp)
       │        mov         %eax,0x14(%rsp)
       │      → call        long java.lang.System::nanoTime()
       │        nop
       │        mov         0x20(%rsp),%rdi
       │        mov         %rax,0x58(%rsp)
       │      → call        char org.sample.strings.CharAt::charAtLatin1()
       │        nop
       │        mov         0x14(%rsp),%edi
       │        test        %edi,%edi
       │      ↓ je          1290
       │        mov         0x28(%rsp),%rsi
       │        cmpb        $0x0,0xb0(%rsi)
       │      ↓ jne         136e
       │        movq        $0x1,0x50(%rsp)
       │        nop
       │ 480:   mov         0x20(%rsp),%rdi
       │      → call        char org.sample.strings.CharAt::charAtLatin1()
       │        nop
       │        mov         0x50(%rsp),%rax
       │        inc         %rax
       │        mov         0x28(%rsp),%rsi
       │        nop
       │        cmpb        $0x0,0xb0(%rsi)
       │      ↓ jne         87d
       │        movl        $0x1,0x74(%rsp)
       │      ↓ jmp         7a4
       │        nop
  0.25 │ 4c0:   mov         %rax,0x40(%rsp)
  0.29 │        mov         0x20(%rsp),%rdi
  0.55 │      → call        char org.sample.strings.CharAt::charAtLatin1()
  0.40 │        nop
  0.22 │        mov         0x48(%rsp),%rax
  0.22 │        lea         0x2(%rax),%rcx
  0.51 │        mov         0x28(%rsp),%rsi
  0.76 │        xchg        %ax,%ax
  0.54 │        cmpb        $0x0,0xb0(%rsi)
  0.47 │      ↓ jne         cf2
  0.84 │        mov         %rcx,0x40(%rsp)
  0.62 │        mov         0x20(%rsp),%rdi
  0.95 │      → call        char org.sample.strings.CharAt::charAtLatin1()
  0.87 │        nop
  0.29 │        mov         0x48(%rsp),%rax
  0.44 │        lea         0x3(%rax),%rcx
  0.76 │        mov         0x28(%rsp),%rsi
  0.40 │        cmpb        $0x0,0xb0(%rsi)
  0.44 │      ↓ jne         cfa
  0.91 │        mov         %rcx,0x40(%rsp)
  0.77 │        mov         0x20(%rsp),%rdi
  2.22 │      → call        char org.sample.strings.CharAt::charAtLatin1()
  1.16 │        nop
  0.51 │        mov         0x48(%rsp),%rax
  0.47 │        lea         0x4(%rax),%rcx
  0.29 │        mov         0x28(%rsp),%rsi
  0.25 │        data16      nopw 0x0(%rax,%rax,1)
  0.62 │        cmpb        $0x0,0xb0(%rsi)
  0.40 │      ↓ jne         d05
  0.55 │        mov         %rcx,0x40(%rsp)
  0.40 │        mov         0x20(%rsp),%rdi
  1.13 │      → call        char org.sample.strings.CharAt::charAtLatin1()
  0.44 │        nop
  0.47 │        mov         0x48(%rsp),%rax
  0.73 │        lea         0x5(%rax),%rcx
  1.60 │        mov         0x28(%rsp),%rsi
  1.24 │        cmpb        $0x0,0xb0(%rsi)
  0.69 │      ↓ jne         d0d
  0.51 │        mov         %rcx,0x40(%rsp)
  0.40 │        mov         0x20(%rsp),%rdi
  1.52 │      → call        char org.sample.strings.CharAt::charAtLatin1()
  0.98 │        nop
  0.58 │        mov         0x48(%rsp),%rax
  0.51 │        lea         0x6(%rax),%rcx
  0.47 │        mov         0x28(%rsp),%rsi
  0.47 │        data16      nopw 0x0(%rax,%rax,1)
  0.69 │        cmpb        $0x0,0xb0(%rsi)
  0.33 │      ↓ jne         d15
  0.40 │        mov         %rcx,0x40(%rsp)
  0.29 │        mov         0x20(%rsp),%rdi
  1.20 │      → call        char org.sample.strings.CharAt::charAtLatin1()
  0.84 │        nop
  0.36 │        mov         0x48(%rsp),%rax
  0.22 │        lea         0x7(%rax),%rcx
  0.98 │        mov         0x28(%rsp),%rsi
  0.51 │        cmpb        $0x0,0xb0(%rsi)
  0.29 │      ↓ jne         d1d
```