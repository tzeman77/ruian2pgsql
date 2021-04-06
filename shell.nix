{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    gnumake
    openjdk8
    postgresql_12
    unzip
    wget
  ];
}
