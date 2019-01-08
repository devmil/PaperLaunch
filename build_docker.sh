#!/bin/bash
set -ex

docker build \
    -t paperlaunch-build \
    .

docker run --rm \
    -v "$PWD":/home/gradle/ \
    -w /home/gradle \
    paperlaunch-build \
    bash .ci/build.sh