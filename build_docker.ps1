docker.exe build -t paperlaunch-build .

# for Docker Toolbox (not sure if Docker would need this)
$path = $PSScriptRoot
$path = $path -replace ':', ''
$path = $path -replace '\\', '/'
$path = $path -replace 'c/Users', '/c/Users'

docker.exe run --rm -it -v ${path}:/home/gradle/ -w /home/gradle paperlaunch-build /bin/bash -c "bash_r .ci/build.sh"
