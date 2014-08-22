## 0.1.4

- Specify command and extra files in project.clj
  Allows config to be given in project.clj with the form :

      :uberimage {:cmd ["/bin/dash" "/myrunscript" "param1" "param2"]
                  :files {"myrunscript" "docker/myrunscript"}}

  - The :cmd value maps directly to a Dockerfile CMD statement

  - The :files value is a map of additional files to be copied into the
   docker image. Keys are docker image target paths and values are lein
   project source paths

  This permits more flexibility in starting the containerized program,
  allowing things such as configuring extra environment with a script.

- Add a CONTRIBUTING.md file

## 0.1.3

- Reference uberjar with an absolute path
  This enables the container working directory to be set to something other
  than /.

- Support %s in :target-path
  Fixes #3

## 0.1.2

- Add options for docker endpoint and base image
  Allow specification of the docker endpoint using the DOCKER_ENDPOINT
  environment variable or the -H command line option.

  Allow specification of the base image with the `-b` comand line option.

## 0.1.1

- Only print generated image id
  On success, only show the resulting image id.

- Update to clj-docker 0.1.1
  Fixes json parsing

- Fix group id in readme

## 0.1.0

- Initial release
