#!/usr/bin/env bash

set -ex

benchmark=$1
java_name="fast"

dir="logs/hotspot-$java_name-$benchmark"
java_home="$HOME/src/jdk/build/$java_name-linux-x86_64/jdk"
java="$java_home/bin/java"
mvn="$HOME/opt/maven/bin/mvn"

mkdir -p $dir

JAVA_HOME=$java_home $mvn clean package -Djvm.mode | tee $dir/mvn-package.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark -jvmArgs -XX:+UnlockDiagnosticVMOptions -jvmArgs -XX:CompileCommand=print,org.sample.strings.*::* | tee $dir/assembly.log
