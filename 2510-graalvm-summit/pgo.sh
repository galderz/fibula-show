#!/usr/bin/env bash

set -ex

benchmark=$1

dir="logs/pgo-$java_name-$benchmark"
java_home="$HOME/opt/ee-graalvm-25"
java="$java_home/bin/java"
mvn="$HOME/opt/maven/bin/mvn"

mkdir -p $dir

JAVA_HOME=$java_home $mvn clean package -Dpgo | tee $dir/mvn-package.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark | tee $dir/benchmark.log

JAVA_HOME=$java_home $mvn clean package -Dpgo.perf | tee $dir/mvn-package-debug.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark -prof org.mendrugo.fibula.PerfDwarfProfiler:events=cycles | tee $dir/profile.log
perf annotate --stdio2 -i ./org.sample.strings.$benchmark.latin1-AverageTime.perfbin > $dir/annotate.log
