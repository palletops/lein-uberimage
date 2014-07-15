# uberimage

A Leiningen plugin to generate a [docker][docker] image that runs a project's uberjar.

## Usage

Put `[com.palletops/uberimage "0.1.1"]` into the `:plugins` vector of your
`:user` profile.

    $ lein uberimage

The plugin will report the uuid of the generated image.

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

## Limitations

Depends on leiningen master branch.  Requires docker api on a TCP
socket.

## TODO

Allow choice of running AOT's with -jar, or non AOT'd with clojure.main.

Allow choice of java runtime.

Use `-t` to specify a tag.

Use `-p` to push the resulting image to a repository.

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[docker]:http://www.docker.com/ Docker
