# 4. Concurrency
Date: 2023-08-28

## Status
Accepted

## Context
How will this allow multiple concurrent clients?

## Decision
This will use [Netty](https://netty.io/4.1/api/index.html), a Java network framework that uses threads.

I tried and failed usto use [core.async](https://clojure.github.io/core.async/) from [0003-concurrency](doc/adr/0003-concurrency.md).

The [CLI](https://docs.redis.com/latest/rs/references/cli-utilities/redis-cli/) hung in interactive mode.

## Consequences
1. This will be easier than writing a custom server. [Netty](https://netty.io/4.1/api/index.html) handles low-level details of networking.
2. Other developers should understand this, as Netty is common in Clojure and Java.
3. Configuring [Netty](https://netty.io/4.1/api/index.html) can be hard, and require low-level knowledge.
