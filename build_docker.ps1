docker.exe build -t paperlaunch-build .

docker.exe run --rm -it -v ${PSScriptRoot}:/home/gradle/ -w /home/gradle paperlaunch-build /bin/bash -c "bash_r .ci/build.sh"
