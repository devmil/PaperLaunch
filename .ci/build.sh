#!/bin/bash
set -ex
gradle build
gradle assembleDebug
gradle assembleRelease
chown -R `stat -c "%u:%g" $(pwd)/app` $(pwd)/app/build
chown -R `stat -c "%u:%g" $(pwd)/app` $(pwd)/build