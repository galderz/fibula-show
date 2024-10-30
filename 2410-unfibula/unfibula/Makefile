SHELL := bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DELETE_ON_ERROR:
MAKEFLAGS += --warn-undefined-variables
MAKEFLAGS += --no-builtin-rules

ifeq ($(origin .RECIPEPREFIX), undefined)
  $(error This Make does not support .RECIPEPREFIX. Please use GNU Make 4.0 or later)
endif
.RECIPEPREFIX = >

GRAALVM_HOME ?= $(HOME)/opt/graal-21
JAVA_HOME ?= $(HOME)/opt/java-21
MAVEN_HOME ?= $(HOME)/opt/maven

benchmarks_jar = target/benchmarks.jar
benchmarks_exec = target/benchmarks
java = $(JAVA_HOME)/bin/java
native_image = $(GRAALVM_HOME)/bin/native-image

mvnw += JAVA_HOME=$(JAVA_HOME)
mvnw += $(MAVEN_HOME)/bin/mvn

default: $(benchmarks_exec)

$(benchmarks_exec): $(benchmarks_jar)
> cd target
> $(native_image) -jar benchmarks.jar

$(benchmarks_jar): $(shell find . -type f -name "*.java" ! -path "./*/target/*")
$(benchmarks_jar): $(shell find . -type f -name "*.json" ! -path "./*/target/*")
$(benchmarks_jar): $(shell find . -type f -name "pom.xml" ! -path "./*/target/*")
> $(mvnw) package

run-jvm: $(benchmarks_jar)
> $(java) -jar $< -f 1 -r 1 -w 1 -i 2 -wi 2
.PHONY: run-jvm
