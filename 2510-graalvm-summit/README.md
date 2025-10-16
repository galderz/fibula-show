# TBD

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

What makes Oracle GraalVM faster?

The `CharAt` source has this structure:

```java
public class CharAt
{
    private String strLatin1;
    private int charAtIndex;
    // ...
}
```

In Oracle GraalVM that gets mapped to a struct like this:

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
   8.34         cmpl       $0x0,0x10(%r15)        ;; isDone check
   0.19       ↑ jg         e0   
```

On the other hand, 


the `perf annotate` output for GraalVM CE demonstrates limited inlining.
...

```bash
Percent       0xcc1b60 <void org.sample.strings.jmh_generated.CharAt_latin1_jmhTest::latin1_avgt_jmhStub(org.openjdk.jmh.runner.InfraControl*, org.openjdk.jmh.results.RawResults*, org.openjdk.jmh.infra.BenchmarkParams*, org.openjdk.jmh.infra.IterationParams*, org.openjdk.jmh.infra.ThreadParams*, org.openjdk.jmh.infra.Blackhole*, org.openjdk.jmh.infra.Control*, org.sample.strings.jmh_generated.CharAt_jmhType*)>:

```