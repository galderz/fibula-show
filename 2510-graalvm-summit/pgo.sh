#!/usr/bin/env bash

set -Eeuox pipefail
trap 'echo "error on line $LINENO"; exit 1' ERR

benchmark=$1
java_name="ee-graalvm-25"

dir="logs/pgo-$java_name-$benchmark"
java_home="${GRAALVM_EE_HOME:-$HOME/opt/$java_name}"
java="$java_home/bin/java"
maven_home="${MAVEN_HOME:-$HOME/opt/maven}"
mvn="$maven_home/bin/mvn"

mkdir -p $dir

$mvn clean package -Dpgo | tee $dir/mvn-package.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark | tee $dir/benchmark.log

$mvn clean package -Dpgo.perf | tee $dir/mvn-package-debug.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark -prof org.mendrugo.fibula.PerfDwarfProfiler:events=cycles | tee $dir/profile.log
perf annotate --stdio2 -i ./org.sample.strings.$benchmark.latin1-AverageTime.perfbin > $dir/annotate.log
gdb -ex "ptype /o 'org.sample.strings.CharAt'" -ex "ptype /o 'java.lang.String'" -ex "ptype /o 'byte[]'" -ex quit ./target/benchmarks > $dir/structs.log
