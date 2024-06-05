todo: adjust running times to be long enough to see differences

Before:

One shell:
maven-21
cd jmh
mvn clean package -DskipTests

Another shell:
graal-21
cd fibula
mvn clean package -DskipTests -Pnative
^ todo consider building it already with debug info et al

Show first JMH benchmark

jmh folder:
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 10 -w 10

Note: VM version, VM invoker

Show first Fibula benchmark

fibula folder:
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 10 -w 10
