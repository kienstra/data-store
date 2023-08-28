# 3. Concurrency
Date: 2023-08-17

## Status
Superseded by [0004-concurrency](0004-concurrency.md)

## Context
How will this allow multiple concurrent clients?

## Decision
This will use Clojure's native [core.async](https://clojure.github.io/core.async/).

## Consequences
1. This wil be easy to understand, as it's the most idiomatic way to do asynchrony in Clojure.
1. New developers will understand right away how [core.async](https://clojure.github.io/core.async/) works.
1. It's much more complex than doing nothing. This already supports multiple concurrent clients.
1. The store will need to be in a [ref](https://clojure.org/reference/refs), which is more complex than the [immutable](https://github.com/kienstra/data-store/blob/f4b57c37d7012db756d4211bdf6d2f5515873d32/src/data_store/server.clj#L11) store.
1. State won't be immutable anymore. It'll be a lot harder to reason about and debug.
1. There might be issues of locking, as a server can have errors and delays.

## Considered and rejected
1. Doing nothing: this already supports multiple clients. But its concurrent handling is very poor: `redis-benchmark` doesn't run at all with more than `-n 50`.
1. Native Java threads, like [java.util.concurrent/Executor](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html). This would work, but it's not idiomatic to Clojure, and is more complex.
1. Clojure [promise](https://clojuredocs.org/clojure.core/promise). This doesn't apply to a server, as there is already a server handler.
