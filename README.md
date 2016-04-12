# uberimage

A Leiningen plugin to generate a [docker](http://www.docker.com/) image that runs a project's uberjar.

Requires _leiningen 2.4.3 or later_.

## Usage

Put `[com.palletops/uberimage "0.4.1"]` into the `:plugins` vector of your
`:user` profile.

    $ lein uberimage

The plugin will run 'uberjar' on your project, generate the docker
image with your uberjar in it, and report the uuid of the generated image.

Note that if you have not already pulled the base image (eg. with
`docker pull pallet/java`), then it might take some while for the
image generation to complete.

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

## Configuration

The `project.clj` `:uberimage` key can be used to configure the
image's `CMD` and to place extra files into the image.

```clj
:uberimage {:cmd ["/bin/dash" "/myrunscript" "param1" "param2"]
            :instructions ["RUN apt-get update && apt-get -y dist-upgrade"]
            :files {"myrunscript" "docker/myrunscript"}
            :tag "user/repo:tag"}
```

The `:cmd` value maps directly to a Dockerfile CMD statement

The `:instructions` value specifies a list of Dockerfile instructions
to be inserted into the generated Dockerfile immediately after
the `FROM` instruction at the start of the Dockerfile

The `:files` value is a map of additional files to be copied into the
docker image. Keys are docker image target paths and values are lein
project source paths.

The `:tag` value supplies a repository name (and optionally a tag) to
be applied to the resulting image in case of success. If `:project-version-tag`
is set to `true`, the Leiningen project version is used as the tag.
In this case, the `:tag` value refers only to the repository name.

The `:base-image` value is used to specify the base image from which
the project image is built (defaults to `pallet/java`).

## Options

By default, the docker API is assumed to be on
`http://localhost:2375`.  You can override this by setting the
the `DOCKER_HOST` environment variable, or using the `-H` option, e.g.

```
lein uberimage -H http://localhost:4243
```

If you have `DOCKER_HOST` set, but don't want that value used,
override it with the `DOCKER_ENDPOINT` environment variable.

The base image used to construct the image can be specified using
`-b`.

```
lein uberimage -b your-image-with-jvm
```

The repository name (and optionally a tag) to be applied to the
resulting image in case of success can be specified using `-t`.

```
lein uberimage -t user/repo:tag
```

The Leiningen project version can be used as the image tag
via the `-p` option.

```
lein uberimage -t user/repo -p
```

## Limitations

Currently your project needs to build with lein uberjar (as `lein
uberimage` invokes `uberjar`) and you must supply a `:main` so that the
uberjar is executable via `java -jar`.

Depends on leiningen 2.4.3 or later.

Using a `:target-path` with a `%s` is it seems to break the plugin.

Requires docker api on a TCP socket (eg. for
[plain docker](https://docs.docker.com/articles/basics/#bind-docker-to-another-hostport-or-a-unix-socket)
or on
[coreos](http://coreos.com/docs/launching-containers/building/customizing-docker/)).
Other examples of enabling the API:
[on ubuntu](http://www.virtuallyghetto.com/2014/07/quick-tip-how-to-enable-docker-remote-api.html).

## TODO

Allow choice of running AOT's with -jar, or non AOT'd with clojure.main.

Allow choice of java runtime.

Use `-p` to push the resulting image to a repository.

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
