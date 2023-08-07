# ADR: Programming Language/Stack
2023-08-06

## Status
Proposed

## Context
What programming language should this data store be in? It should have good support for network programming and Test Driven Development.

## Decision
This project will be in [Clojure](https://clojure.org/).

## Consequences
1. Interoperability will make this easier, as this project can use [Clojure](https://redis.io/resources/clients/#clojure) and [Java](https://redis.io/resources/clients/) libraries. Clojure [can run](https://clojure.org/reference/java_interop) Java.
2. There are good server libraries, like [httpkit.server](https://http-kit.github.io/http-kit/org.httpkit.server.html) and [compojure](http://weavejester.github.io/compojure/compojure.core.html).
3. There's a good [testing library](https://clojure.github.io/clojure/clojure.test-api.html) and [server mocking library](https://github.com/ring-clojure/ring-mock).
4. Clojure is mainly immutable, but you can set [atoms](https://clojure.org/reference/atoms) to persist data in the data store.
5. Long-term support is good. Clojure is [16 years old](https://en.wikipedia.org/wiki/Clojure), and will probably be supported for a long time.
6. You don't have to keep up with new releases much. The Clojure language last released a new version [over a year ago](https://clojure.org/releases/downloads). There will be little need to keep up with language and library releases.
7. Requires less development time. Clojure is terse, and has mainly pure functions. This promotes fast, Test Driven Development.
8. Hosting costs should be the same as other languages. You can package a Clojure app [as a JAR file](https://www.braveclojure.com/java/), so hosting could be the same as for a Java app.
9. There is a small but [helpful](https://clojure.org/news/2022/06/02/state-of-clojure-2022) community. Searching for common Clojure questions usually shows an answer on [Stack Overflow](https://stackoverflow.com/) or blogs.
9. Ability to hire is not as good as other languages. There are fewer Clojure developers than other languages, so we'd probably have to train developers on Clojure.
10. Lock-in is more than other languages. This isn't vendor lock-in, but it's hard to port Clojure to a language like Python or TypeScript, because it's so different. If this were a bigger project, it'd be a big concern.
