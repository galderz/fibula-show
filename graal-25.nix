let
  stable = import (builtins.fetchTarball
    "https://github.com/NixOS/nixpkgs/archive/nixos-25.05.tar.gz") { };

  unstable = import (builtins.fetchTarball
    "https://github.com/NixOS/nixpkgs/archive/nixos-unstable.tar.gz") { };
in
stable.mkShell {
  packages = with stable; [
    git
    maven
    zsh
  ] ++ [
    unstable.graalvm-ce
    unstable.graalvm-oracle
  ];

  GRAALVM_EE_HOME="${pkgs.graalvmPackages.graalvm-oracle}";
  JAVA_HOME="${pkgs.graalvmPackages.graalvm-ce}";
  MAVEN_HOME="${pkgs.maven}";
  NATIVE_IMAGE_OPTIONS="-H:-CheckToolchain";

  shellHook = ''
    echo "GRAALVM_EE_HOME set to $GRAALVM_EE_HOME"
    echo "JAVA_HOME set to $JAVA_HOME"
    echo "MAVEN_HOME set to $MAVEN_HOME"
    echo "NATIVE_IMAGE_OPTIONS set to $NATIVE_IMAGE_OPTIONS"
  '' ;
}
