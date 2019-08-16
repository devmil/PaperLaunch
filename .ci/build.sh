#!/bin/bash
#set -xeuo pipefail
set -x pipefail

source_root=/home/app

echo copying code to build to /tmp/Paperlaunch

if [ -d /tmp/Paperlaunch ]; then
  rm -rf /tmp/Paperlaunch
fi
cp -r $source_root /tmp/Paperlaunch
cd /tmp/Paperlaunch

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

if [ $cmdresult -eq 0 ]; then
  if [ -d $source_root/app/build ]; then
    rm -rf $source_root/app/app/build
  fi
  cp -r $(pwd)/app/build $source_root/app/build
  cd $source_root
  chown -R `stat -c "%u:%g" $(pwd)/app` $(pwd)/app/build
fi

# return the build result
exit $cmdresult
