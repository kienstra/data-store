# 0002. Data Structures
Date: 2023-08-28

## Status
Accepted

## Context
What data structures should this store use? They should perform well at 100,000 requests per second.

## Decision
This will use Clojure data structures: [hash maps](https://clojure.org/reference/data_structures#Maps) and [Vectors](https://clojure.org/reference/data_structures#Vectors).

The store will be a simple [hash map](https://clojure.org/reference/data_structures#Maps):

```
{"Name" {:val "John" :exp 100100}}
```

…not a [protocol](https://clojure.org/reference/protocols).

## Consequences
1. The [hash map](https://clojure.org/reference/data_structures#Maps) should perform better than wrapping a map in a [protocol](https://clojure.org/reference/protocols).
1. The shape of the hash map becomes important. For example, with `{"Name" {:val "John"}}`, functions will need to know that the value is at `:val`. If it were wrapped in a [protocol](https://clojure.org/reference/protocols), it could have getters and setters that would hide those details.
1. Clojure [vectors](https://clojure.org/reference/data_structures#Vectors) should perform well for [lists](https://redis.io/docs/data-types/lists/). Getting their [length](https://clojure.org/reference/data_structures#Vectors) is 0(1).
