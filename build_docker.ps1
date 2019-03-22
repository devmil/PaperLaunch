docker.exe build -t paperlaunch-build .

# for Docker Toolbox (not sure if Docker would need this)
$path = $PSScriptRoot
$path = $path -replace ':', ''
$path = $path -replace '\\', '/'
$path = $path -replace 'c/Users', '/c/Users'

docker.exe run --rm -it -v ${path}:/home/gradle/ -w /home/gradle paperlaunch-build /bin/bash -c "/usr/bin/dos2unix -n .ci/build.sh .ci/build_lf.sh && bash .ci/build_lf.sh && rm .ci/build_lf.sh"
