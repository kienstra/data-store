# ADR: Programming Language/Stack
2023-08-06

## Status
Proposed

## Context
What programming language should this data store be in? It should have good support for network programming and Test-Driven Development.

## Decision
This project will be in [Clojure](https://clojure.org/).

## Consequences
1. Interoperability will make this easier, as this project can use [Clojure](https://redis.io/resources/clients/#clojure) and [Java](https://redis.io/resources/clients/) libraries. Clojure [can run](https://clojure.org/reference/java_interop) Java.
2. There are good server libraries, like [httpkit.server](https://http-kit.github.io/http-kit/org.httpkit.server.html) and [compojure](http://weavejester.github.io/compojure/compojure.core.html).
3. There's a good [testing library](https://clojure.github.io/clojure/clojure.test-api.html) and [server mocking library](https://github.com/ring-clojure/ring-mock).
4. Clojure is mainly immutable, but you can set [atoms](https://clojure.org/reference/atoms) to persist data in the data store.
