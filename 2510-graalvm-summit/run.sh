#!/usr/bin/env bash

set -ex

rm -drf logs

./aot.sh CharAt graalvm-25
./aot.sh CharAt ee-graalvm-25
./hotspot.sh CharAt

./aot.sh DontInlineCharAt graalvm-25
./aot.sh DontInlineCharAt ee-graalvm-25
./pgo.sh DontInlineCharAt
./hotspot.sh DontInlineCharAt

./aot.sh LoadArrayCharAt graalvm-25
./aot.sh LoadArrayCharAt ee-graalvm-25
./pgo.sh LoadArrayCharAt
./hotspot.sh LoadArrayCharAt
