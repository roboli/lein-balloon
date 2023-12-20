# lein-balloon

A Leiningen plugin to run the [balloon](https://github.com/roboli/balloon) library as a CLI.

## Quickstart

For installation, add it as a plugin to your `project.clj` file or your global profile:

```
:plugins [[org.clojars.roboli/lein-balloon "0.1.0-SNAPSHOT"]]
```

Run deflate to flat a nested hash-ma:

```
$ lein balloon deflate '{:a {:b "c"}}' :delimiter '*'

;;=> {:a*b c}
```

Run inflate to convert a deflated (flatten) hash-map into a json nested object:

```
$ lein balloon inflate '{:a*b "c"}' :delimiter '*' -T json

;;=> {"a":{"b":"c"}}
```

## Usage

Print help:

```
$ lein balloon -h

;;=>
Usage: command [arguments] [options]

Commands:
  deflate  Flat a nested hash-map
  inflate  Unflat hash-map into a nested one

Options:
  -t, --input-type FORMAT         edn  Input format in edn or json
  -T, --output-type FORMAT        edn  Output format in edn or json
  -f, --file NAME                      File with value to read
  -c, --input-clipboard BOOLEAN        Use value from clipboard
  -C, --output-clipboard BOOLEAN       Use value from clipboard
  -h, --help

Examples: lein balloon inflate "{:a.b \"c\"}"
          lein balloon deflate "{:a {:b \"c\"}}" :delimiter "*"
```

Examples:

## License

Copyright © 2023 Roberto Oliveros

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
