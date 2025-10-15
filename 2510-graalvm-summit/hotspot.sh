#!/usr/bin/env bash

set -ex

benchmark=$1

dir="logs/hotspot=$java_name-$benchmark"
java_home="$HOME/opt/java-25"
java="$java_home/bin/java"
mvn="$HOME/opt/maven/bin/mvn"

mkdir -p $dir

JAVA_HOME=$java_home $mvn clean package -Djvm.mode | tee $dir/mvn-package.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark | tee $dir/benchmark.log
$java -jar target/benchmarks.jar org.sample.strings.$benchmark -prof perfasm | tee $dir/perfasm.log
