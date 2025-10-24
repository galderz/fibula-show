#!/usr/bin/env bash

set -Eeuox pipefail
trap 'echo "error on line $LINENO"; exit 1' ERR

benchmark=$1
java_name="java-25"

dir="logs/hotspot-$java_name-$benchmark"
fast_java_home="$HOME/src/jdk/build/fast-linux-x86_64/jdk"
fast_java="$fast_java_home/bin/java"
java_home="$HOME/src/jdk/build/release-linux-x86_64/jdk"
java="$java_home/bin/java"
maven_home="${MAVEN_HOME:-$HOME/opt/maven}"
mvn="$maven_home/bin/mvn"

mkdir -p $dir

JAVA_HOME=${java_home} $mvn clean package -Djvm.mode | tee $dir/mvn-package.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark | tee $dir/benchmark.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark -prof perfasm:hotThreshold=0.001 | tee $dir/perfasm.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark -jvmArgs -XX:+UnlockDiagnosticVMOptions -jvmArgs -XX:CompileCommand=print,org.sample.strings.*::* | tee $dir/assembly-release.log
$fast_java -jar target/benchmarks.jar org.sample.strings.$benchmark -jvmArgs -XX:+UnlockDiagnosticVMOptions -jvmArgs -XX:CompileCommand=print,org.sample.strings.*::* | tee $dir/assembly-fastdebug.log
