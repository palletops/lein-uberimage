# uberimage

A Leiningen plugin to generate a [docker][docker] image that runs a project's uberjar.

## Usage

Put `[uberimage "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
uberimage 0.1.0-SNAPSHOT`.

    $ lein uberimage

The plugin will report the uuid of the generated image.

## Limitations

Depends on leiningen master branch.

## TODO

Allow specification of docker endpoint.

Allow choice of running AOT's with -jar, or non AOT'd with clojure.main.

Allow choice of java runtime.

Allow choice of base image.

Use `-t` to specify a tag.

Use `-p` to push the resulting image to a repository.

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[docker]:http://www.docker.com/ Docker
