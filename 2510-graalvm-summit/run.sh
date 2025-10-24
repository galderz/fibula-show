#!/usr/bin/env bash

set -Eeuox pipefail
trap 'echo "error on line $LINENO"; exit 1' ERR

rm -drf logs

JAVA_HOME=${GRAALVM_EE_HOME} ./aot.sh CharAt ee-graalvm-25
JAVA_HOME=${JAVA_HOME} ./aot.sh CharAt graalvm-25
JAVA_HOME=${JDK25_HOME} ./hotspot.sh CharAt

JAVA_HOME=${GRAALVM_EE_HOME} ./aot.sh DontInlineCharAt ee-graalvm-25
JAVA_HOME=${GRAALVM_EE_HOME} ./pgo.sh DontInlineCharAt
JAVA_HOME=${JAVA_HOME} ./aot.sh DontInlineCharAt graalvm-25
JAVA_HOME=${JDK25_HOME} ./hotspot.sh DontInlineCharAt

JAVA_HOME=${GRAALVM_EE_HOME} ./aot.sh LoadArrayCharAt ee-graalvm-25
JAVA_HOME=${GRAALVM_EE_HOME} ./pgo.sh LoadArrayCharAt
JAVA_HOME=${JAVA_HOME} ./aot.sh LoadArrayCharAt graalvm-25
JAVA_HOME=${JDK25_HOME} ./hotspot.sh LoadArrayCharAt
