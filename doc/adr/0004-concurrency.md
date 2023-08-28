# 0004. Concurrency
Date: 2023-08-28

## Status
Accepted

## Context
How will this allow multiple concurrent clients?

## Decision
This will use [Netty](https://netty.io/4.1/api/index.html), a Java networking framework that uses threads.

I tried and failed to use [core.async](https://clojure.github.io/core.async/) from [0003-concurrency](0003-concurrency.md).

The [CLI](https://docs.redis.com/latest/rs/references/cli-utilities/redis-cli/) hung in interactive mode.

## Consequences
1. This will be easier than writing a custom server. [Netty](https://netty.io/4.1/api/index.html) handles low-level details of networking.
1. Other developers should understand this, as Netty is common in Clojure and Java.
1. Configuring [Netty](https://netty.io/4.1/api/index.html) can be hard, and require low-level knowledge. Still, writing a custom TCP server would be much harder, and it's not clear it would perform better.
1. The threads should perform well. Netty configures the amount of threads so well that setting a custom number doesn't improve the performance.
