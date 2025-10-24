let
  stable = import <nixpkgs> { };

  unstable = import (builtins.fetchTarball
    "https://github.com/NixOS/nixpkgs/archive/nixos-unstable.tar.gz") {
      config = {
        allowUnfree = true;
      };
    };
in
stable.mkShell {
  pakages = with stable; [
    gdb
    git
    maven
    pahole
    perf-tools
    zsh
  ] ++ [
    unstable.graalvmPackages.graalvm-ce
    unstable.graalvmPackages.graalvm-oracle
    unstable.temurin-bin-25
  ];

  GRAALVM_EE_HOME="${unstable.graalvmPackages.graalvm-oracle}";
  JAVA_HOME="${unstable.graalvmPackages.graalvm-ce}";
  JDK25_HOME="${unstable.temurin-bin-25}";
  MAVEN_HOME="${stable.maven}";
  NATIVE_IMAGE_OPTIONS="-H:-CheckToolchain";

  shellHook = ''
    echo "GRAALVM_EE_HOME set to $GRAALVM_EE_HOME"
    echo "JAVA_HOME set to $JAVA_HOME"
    echo "JDK25_HOME set to $JDK25_HOME"
    echo "MAVEN_HOME set to $MAVEN_HOME"
    echo "NATIVE_IMAGE_OPTIONS set to $NATIVE_IMAGE_OPTIONS"
  '' ;
}
