# erdos.kanren

A Cl
A Kanren implementation with Transducers in Clojure.

## Usage

FIXME

## Performance

The [Zebra Puzzle](https://en.wikipedia.org/wiki/Zebra_Puzzle) was used for benchmarking the code. You can also run the tests with `$ lein test :bench` with [Leiningen](https://leiningen.org/).


--------------------------------------------------------
|    Library               | Mean Time | Std-deviation |
--------------------------------------------------------
| erdos.kanren             | 13.28 ms  | 148 µs        |
| clojure.core.logic 1.0.0 | 21.52 ms  | 207 µs        |


## Bibliography

- [µKanren: A Minimal Functional Core for Relational Programming](http://webyrd.net/scheme-2013/papers/HemannMuKanren2013.pdf)
- [core.logic](https://github.com/clojure/core.logic), miniKanren in Clojure
- [MicroLogic](http://mullr.github.io/micrologic/literate.html) literate documentation

## License

Copyright © 2020 Janos Erdos

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
