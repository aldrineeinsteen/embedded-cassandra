# Embedded Cassandra [![Build Status OSX/Linux](https://img.shields.io/travis/nosan/embedded-cassandra/master.svg?logo=travis&logoColor=white&style=flat)](https://travis-ci.org/nosan/embedded-cassandra) [![Build Status Windows](https://img.shields.io/appveyor/ci/nosan/embedded-cassandra/master.svg?logo=appveyor&logoColor=white&style=flat)](https://ci.appveyor.com/project/nosan/embedded-cassandra)
Embedded Cassandra provides an easy way to start and stop [Apache Cassandra](https://cassandra.apache.org/) as an embedded database. Apache Cassandra is not started within the same JVM, but instead that, it is started as a forked JVM process (`java.lang.Process`).

To learn more about Embedded Cassandra, please consult the [reference documentation](https://nosan.github.io/embedded-cassandra/).

Embedded Cassandra _2.x.x_ documentation is [here](https://github.com/nosan/embedded-cassandra/wiki)

## Issues

`Embedded Cassandra` uses GitHub's issue tracking system to report bugs and feature
requests. If you want to raise an issue, please follow this [link](https://github.com/nosan/embedded-cassandra/issues)

Also see [CONTRIBUTING.md](CONTRIBUTING.md) if you wish to submit pull requests.

## Build

`Embedded Cassandra` can be easily built with the [maven wrapper](https://github.com/takari/maven-wrapper). You also need `JDK 1.8`.

## License

Embedded Cassandra is released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
