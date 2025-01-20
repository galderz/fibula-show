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

JAVA_HOME ?= $(HOME)/opt/java-21
CE_HOME ?= $(HOME)/opt/graal-21
ORACLE_HOME ?= $(HOME)/opt/ee-graal-21
MAVEN_HOME ?= $(HOME)/opt/maven

benchmarks_jar = target/benchmarks.jar
mvn += $(MAVEN_HOME)/bin/mvn
strings_dir := 2412-strings
strings_path = ../$(strings_dir)

all: hotspot-jit graal-jit aot pgo
.PHONY: all

hotspot-jit:
> cd $(strings_path)
> JAVA_HOME=$(JAVA_HOME) $(mvn) clean package -Djvm.mode
> $(JAVA_HOME)/bin/java -jar $(benchmarks_jar)
.PHONY: hotspot-jit

graal-jit:
> cd $(strings_path)
> JAVA_HOME=$(ORACLE_HOME) $(mvn) clean package -Djvm.mode
> $(ORACLE_HOME)/bin/java -jar $(benchmarks_jar)
.PHONY: graal

aot:
> cd $(strings_path)
> JAVA_HOME=$(CE_HOME) $(mvn) clean package
> $(GRAALVM_CE_HOME)/bin/java -jar $(benchmarks_jar)
.PHONY: aot

pgo:
> cd $(strings_path)
> JAVA_HOME=$(ORACLE_HOME) $(mvn) clean package -Dpgo
> $(ORACLE_HOME)/bin/java -jar $(benchmarks_jar)
.PHONY: pgo
