# Never Inline

NOT YET WORKING!

## Get native results

Build:

```shell
mvn package
```

Then just run:
```shell
java -jar target/benchmarks.jar
```

You can get trace inlining information (fails with GraalVM for DK 21 so use say GraalVM for JDK 23):

```shell
mvn package -DbuildArgs=-H:+TraceInlining
```
