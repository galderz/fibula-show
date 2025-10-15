#!/usr/bin/env bash

set -ex

java_home="$HOME/opt/graalvm-25"
java="$java_home/bin/java"
mvn="JAVA_HOME=$java_home $HOME/opt/maven/bin/mvn"

$mvn clean package
$java -jar target/benchmarks.jar org.sample.strings.CharAt.charAtLatin1

$mvn clean package -Ddebug=true -DbuildArgs=-H:-DeleteLocalSymbols,-H:+SourceLevelDebug,-H:+TrackNodeSourcePosition,-H:+DebugCodeInfoUseSourceMappings
java -jar target/benchmarks.jar org.sample.strings.CharAt.charAtLatin1 -prof org.mendrugo.fibula.PerfDwarfProfiler:events=cycles
