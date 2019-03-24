#!/bin/bash
#set -xeuo pipefail
set -x pipefail

# rename local properties if they are present (e.g. when the docker build is started locally)
if [ -f ./local.properties ]; then
    mv ./local.properties ./local.properties.tmp
fi

# build scope
(
    set -e
    gradle test
    gradle assembleDebug
    gradle assembleRelease
    chown -R `stat -c "%u:%g" $(pwd)/app` $(pwd)/app/build
    chown -R `stat -c "%u:%g" $(pwd)/app` $(pwd)/build
)

cmdresult=$?

# rename the local properties back after the build is finished
if [ -f ./local.properties.tmp ]; then
    mv ./local.properties.tmp ./local.properties
fi

# return the build result
exit $cmdresult
