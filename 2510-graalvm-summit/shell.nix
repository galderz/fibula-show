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
  packages = with stable; [
    gdb
    git
    maven
    pahole
    zsh
  ] ++ [
    unstable.graalvmPackages.graalvm-ce
    unstable.graalvmPackages.graalvm-oracle
  ];

  GRAALVM_CE_HOME="${unstable.graalvmPackages.graalvm-ce}";
  GRAALVM_EE_HOME="${unstable.graalvmPackages.graalvm-oracle}";
  MAVEN_HOME="${stable.maven}";
  NATIVE_IMAGE_OPTIONS="-H:-CheckToolchain";

  shellHook = ''
    echo "GRAALVM_CE_HOME set to $GRAALVM_CE_HOME"
    echo "GRAALVM_EE_HOME set to $GRAALVM_EE_HOME"
    echo "MAVEN_HOME set to $MAVEN_HOME"
    echo "NATIVE_IMAGE_OPTIONS set to $NATIVE_IMAGE_OPTIONS"
  '' ;
}
