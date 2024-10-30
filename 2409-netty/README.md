# Netty Buffer Polymorphism

Build for benchmark numbers:
```shell
graal-21
mvn clean package -DskipTests -Dnative -Dquarkus.native.enable-reports -Dquarkus.native.additional-build-args=-H:PrintAnalysisCallTreeType=TXT
```

Run for numbers:
```shell
java -jar target/benchmarks.jar
```

Build for perf:
```shell
graal-21
mvn clean package -DskipTests -Dnative -Dquarkus.native.enable-reports -Dquarkus.native.debug.enabled -Dquarkus.native.additional-build-args=-H:PrintAnalysisCallTreeType=TXT,-H:-DeleteLocalSymbols
```

Run for perf:
```shell
java -jar target/benchmarks.jar -f 1 -prof org.mendrugo.fibula.bootstrap.DwarfPerfAsmProfiler:events=cycles:P
```

Generate compiler graphs:
```shell
mvn clean package -DskipTests -Dnative -Dquarkus.native.enable-reports -Dquarkus.native.debug.enabled "-Dquarkus.native.additional-build-args=-H:PrintAnalysisCallTreeType=TXT,-H:-DeleteLocalSymbols,-H:Dump=:2,-H:MethodFilter=setLongLE*"
```

Generate compiler graphs on macos with latest GraalVM:
```shell
mvn clean package -DskipTests -Dnative "-Dquarkus.native.additional-build-args=-H:Dump=:2,-H:MethodFilter=setLongLE*,-Ddebug.jdk.graal.jvmciConfigCheck=warn"
```
