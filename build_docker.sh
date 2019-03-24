#!/bin/bash

set -ex

docker build \
    -t paperlaunch-build \
    .

docker run --rm \
    -it \
    -v "$PWD":/home/gradle/ \
    -w /home/gradle \
    paperlaunch-build \
    bash_r .ci/build.sh