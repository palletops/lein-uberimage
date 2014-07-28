# uberimage

A Leiningen plugin to generate a [docker](http://www.docker.com/) image that runs a project's uberjar.

## Usage

Put `[com.palletops/uberimage "0.1.2"]` into the `:plugins` vector of your
`:user` profile.

    $ lein uberimage

The plugin will run 'uberjar' on your project, generate the docker
image with your uberjar in it, and report the uuid of the generated image.

## Options

By default, the docker API is assumed to be on
`http://localhost:2375`.  You can override this by setting the
`DOCKER_ENDPOINT` environment variable, or using the `-H` option, e.g.

```
lein uberimage -H http://localhost:4243
```

The base image used to construct the image can be specified using
`-b`.

```
lein uberimage -b your-image-with-jvm
```

## Running your container

Once the image is built, you can run it via docker with

```
docker run generated-image-uuid
```

By default your image doesn't have any ports mapped. If your service
needs to open incoming ports, you need to bind the container port to
a host port, running your container this way instead:

```
docker run generated-image-uuid -p 3000:8080
```

where the `3000` is the port where your service listens on
the container, and the `8080` is the port you want your service
to listen to on the host. Then, open your browser and type
`http://<docker-host-ip>:8080/...` to access your service.

## Limitations

Currently your project needs to build with lein uberjar (as `lein
uberimage` invokes `uberjar`) and you must supply a `:main` so that the
uberjar is executable via `java -jar`.

Depends on leiningen master branch (specifically requires commit
[2cfca444](https://github.com/technomancy/leiningen/commit/2cfca444fe37135637a4efbe9f004d4ce5fe51c7)).
See
[leiningen contributing docs](https://github.com/technomancy/leiningen/blob/master/CONTRIBUTING.md#user-content-bootstrapping)
for how to run leiningen from master.

Requires docker api on a TCP
socket (eg. for
[plain docker](https://docs.docker.com/articles/basics/#bind-docker-to-another-hostport-or-a-unix-socket)
or on
[coreos](http://coreos.com/docs/launching-containers/building/customizing-docker/)).

## TODO

Allow choice of running AOT's with -jar, or non AOT'd with clojure.main.

Allow choice of java runtime.

Use `-t` to specify a tag.

Use `-p` to push the resulting image to a repository.

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
