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

native_image_opts += --no-fallback

mvnw += JAVA_HOME=$(JAVA_HOME)
mvnw += $(MAVEN_HOME)/bin/mvn

java_opts = 
ifdef NATIVE_AGENT
  java = $(GRAALVM_HOME)/bin/java
  java_opts += -agentlib:native-image-agent=config-output-dir=target/native-agent-config-{pid}-{datetime}
else
  java = $(JAVA_HOME)/bin/java
endif

run: $(benchmarks_exec)
> $<

$(benchmarks_exec): $(benchmarks_jar)
> $(native_image) $(native_image_opts) -jar $<

$(benchmarks_jar): $(shell find . -type f -name "*.java" ! -path "./*/target/*")
$(benchmarks_jar): $(shell find . -type f -name "*.json" ! -path "./*/target/*")
$(benchmarks_jar): $(shell find . -type f -name "pom.xml" ! -path "./*/target/*")
> $(mvnw) package

run-jvm: $(benchmarks_jar)
> $(java) $(java_opts) -jar $< -f 1 -r 1 -w 1 -i 2 -wi 2
.PHONY: run-jvm

clean:
> $(mvnw) clean
.PHONY: clean
