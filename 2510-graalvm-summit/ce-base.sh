#!/usr/bin/env bash

set -ex

java_home="$HOME/opt/graalvm-25"
java="$java_home/bin/java"
mvn="$HOME/opt/maven/bin/mvn"
benchmark="CharAt"
dir="logs/ce-base"

mkdir -p $dir

JAVA_HOME=$java_home $mvn clean package | tee $dir/mvn-package.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark | tee $dir/benchmark.log

JAVA_HOME=$java_home $mvn clean package -Ddebug=true -DbuildArgs=-H:-DeleteLocalSymbols,-H:+SourceLevelDebug,-H:+TrackNodeSourcePosition,-H:+DebugCodeInfoUseSourceMappings | tee $dir/mvn-package-debug.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark -prof org.mendrugo.fibula.PerfDwarfProfiler:events=cycles | tee $dir/profile.log
perf annotate --stdio2 -i ./org.sample.strings.CharAt.latin1-AverageTime.perfbin > $dir/annotate.log
