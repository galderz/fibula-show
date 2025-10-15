#!/usr/bin/env bash

set -ex

java_home="$HOME/opt/graalvm-25"
java="$java_home/bin/java"
mvn="$HOME/opt/maven/bin/mvn"
benchmark="CharAt"

JAVA_HOME=$java_home $mvn clean package
$java -jar target/benchmarks.jar org.sample.strings.$benchmark

JAVA_HOME=$java_home $mvn clean package -Ddebug=true -DbuildArgs=-H:-DeleteLocalSymbols,-H:+SourceLevelDebug,-H:+TrackNodeSourcePosition,-H:+DebugCodeInfoUseSourceMappings
$java -jar target/benchmarks.jar org.sample.strings.$benchmark -prof org.mendrugo.fibula.PerfDwarfProfiler:events=cycles
