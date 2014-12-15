## 0.4.0

- Add support for https and auth identity
  Allows use of DOCKER_CERT_PATH for TLS credentials to use for DOCKER_HOST,
  as used by boot2docker.

  This also allows for DOCKER_AUTH=identity, as used by nodes started by 
  docker machine.

- Remove core.async dependency
  Fixes #21

- Use DOCKER_CERT_PATH to find client cert

- Use DOCKER_HOST to specify endpoint
  For consistency with boot2docker, use the DOCKER_HOST environment variable
  to specify the endpoint.  DOCKER_ENDPOINT will still override DOCKER_HOST.

- Show the tag in 'Built image' output, if supplied.

## 0.3.0

- Include additional instructions in Dockerfiles
  Allow the insertion of additional Dockerfile instructions

  The :instructions value is taken from the :uberimage options map in
  project.clj and specifies a list of Dockerfile instructions to be inserted
  immediately after the FROM instruction at the start of the Dockerfile.

## 0.2.0

- Add comment on pulling base image to README
  Addresses #11

- Add extra link to readme on docker api setup
  Closes #12

- Allow :base-image to be specified in project.clj
  Closes #13.

- Update to clj-docker 0.1.2

## 0.1.5

- Allow specifying a repository and tag for generated images
  Can be specified through the `-t` command line option or the
  `[:uberimage :tag]` project key.

- Permit the copying of directories to the image

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
