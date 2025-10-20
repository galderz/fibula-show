#!/usr/bin/env bash

set -ex

benchmark=$1
java_name=$2

dir="logs/$java_name-$benchmark"
java_home="$HOME/opt/$java_name"
java="$java_home/bin/java"
mvn="$HOME/opt/maven/bin/mvn"

mkdir -p $dir

JAVA_HOME=$java_home $mvn clean package | tee $dir/mvn-package.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark | tee $dir/benchmark.log

JAVA_HOME=$java_home $mvn clean package -Ddebug=true -DbuildArgs=-H:-DeleteLocalSymbols,-H:+SourceLevelDebug,-H:+TrackNodeSourcePosition,-H:+DebugCodeInfoUseSourceMappings | tee $dir/mvn-package-debug.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark -prof org.mendrugo.fibula.PerfDwarfProfiler:events=cycles | tee $dir/profile.log
perf annotate --stdio2 -i ./org.sample.strings.$benchmark.latin1-AverageTime.perfbin > $dir/annotate.log
gdb -ex "ptype /o 'org.sample.strings.CharAt'" -ex "ptype /o 'java.lang.String'" -ex "ptype /o 'byte[]'" -ex quit ./target/benchmarks > $dir/structs.log
