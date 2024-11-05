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
mvnw += GRAALVM_HOME=$(GRAALVM_HOME)
mvnw += $(MAVEN_HOME)/bin/mvn

java_opts = 
ifdef NATIVE_AGENT
  java = $(GRAALVM_HOME)/bin/java
  java_opts += -agentlib:native-image-agent=config-output-dir=target/native-config-agent-{pid}
else
  java = $(JAVA_HOME)/bin/java
endif

benchmarks_args += -jar
benchmarks_args += $(benchmarks_jar)
benchmarks_args += -f
benchmarks_args += 1
benchmarks_args += -r
benchmarks_args += 1
benchmarks_args += -w
benchmarks_args += 1
benchmarks_args += -i
benchmarks_args += 2
benchmarks_args += -wi
benchmarks_args += 2

run: $(benchmarks_exec)
> $(java) $(java_opts) $(benchmarks_args)

$(benchmarks_exec): $(benchmarks_jar)

$(benchmarks_jar): $(shell find . -type f -name "*.java" ! -path "./*/target/*")
$(benchmarks_jar): $(shell find . -type f -name "*.json" ! -path "./*/target/*")
$(benchmarks_jar): $(shell find . -type f -name "pom.xml" ! -path "./*/target/*")
> $(mvnw) package -Dnative

run-jvm: $(benchmarks_jar)
> $(java) $(java_opts) $(benchmarks_args)
.PHONY: run-jvm

clean:
> $(mvnw) clean
.PHONY: clean

native_image_configure = $(GRAALVM_HOME)/bin/native-image-configure
native_config_merged_dir = src/main/resources/META-INF/native-image
native_config_forked_agent_dir = src/main/resources/forked-agent-native-config
native_config_forked_additional_dir = src/main/resources/forked-additional-native-config

copy-native-config-agent-runner:
> mkdir -p $(native_config_runner_agent_dir)
> @runner_dir=$$(ls -d target/native-config-agent-* | sort -V | head -n 1)
> cp -r $$runner_dir/* $(native_config_runner_agent_dir)

copy-native-config-agent-forked:
> mkdir -p $(native_config_forked_agent_dir)
> @forked_dir=$$(ls -d target/native-config-agent-* | sort -V | head -n 2 | tail -n 1)
> cp -r $$forked_dir/* $(native_config_forked_agent_dir)

merge-native-config:
> mkdir -p $(native_config_merged_dir)
> $(native_image_configure) \
>   generate \
>   --input-dir=$(native_config_forked_agent_dir) \
>   --input-dir=$(native_config_forked_additional_dir) \
>   --output-dir=$(native_config_merged_dir)
.PHONY: merge-native-config
