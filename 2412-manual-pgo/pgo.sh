#!/usr/bin/env bash

set -e -u -x

mvn package -Dpgo
java -jar target/benchmarks.jar charAtLatin1 -f 1
native-image --bundle-apply=target/benchmarks.nib --bundle-create=target/benchmarks-optimized.nib,dry-run --pgo
native-image --bundle-apply=target/benchmarks-optimized.nib
java -jar target/benchmarks.jar charAtLatin1 -f 1
