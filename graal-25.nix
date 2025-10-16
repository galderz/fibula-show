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
    git
    maven
    pahole
    zsh
  ] ++ [
    unstable.graalvmPackages.graalvm-ce
    unstable.graalvmPackages.graalvm-oracle
  ];

  GRAALVM_EE_HOME="${unstable.graalvmPackages.graalvm-oracle}";
  JAVA_HOME="${unstable.graalvmPackages.graalvm-ce}";
  MAVEN_HOME="${stable.maven}";
  NATIVE_IMAGE_OPTIONS="-H:-CheckToolchain";

  shellHook = ''
    echo "GRAALVM_EE_HOME set to $GRAALVM_EE_HOME"
    echo "JAVA_HOME set to $JAVA_HOME"
    echo "MAVEN_HOME set to $MAVEN_HOME"
    echo "NATIVE_IMAGE_OPTIONS set to $NATIVE_IMAGE_OPTIONS"
  '' ;
}
