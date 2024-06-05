Before:

maven-21
cd jmh
mvn clean install -DskipTests

Show first JMH benchmark

cd jmh
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 10 -w 10

Note: VM version, VM invoker

cd fibula
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 10 -w 10
