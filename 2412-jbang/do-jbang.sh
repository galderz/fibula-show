#!/usr/bin/env bash

set -e -u -x

jbang \
  --compile-option=-processor \
  --compile-option=org.mendrugo.fibula.generator.NativeAssetsGenerator \
  --deps org.openjdk.jmh:jmh-generator-annprocess:1.37 \
  --deps org.mendrugo.fibula:fibula-generator:999-SNAPSHOT \
  --java 21 \
  --main org.mendrugo.fibula.MutiVmMain \
  --native \
  --verbose \
  https://gist.github.com/galderz/fc860d6134cc3061c8e61e1e69c8dec6
