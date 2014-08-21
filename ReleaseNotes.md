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
