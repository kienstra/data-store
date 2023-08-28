# 0002. Data Structures
Date: 2023-08-28

## Status
Accepted

## Context
What data structures should this store use? They should perform well at 100,000 requests per second.

## Decision
This will use Clojure data structures: [hash maps](https://clojure.org/reference/data_structures#Maps) and [vectors](https://clojure.org/reference/data_structures#Vectors).

The store will be a simple [hash map](https://clojure.org/reference/data_structures#Maps):

```
{"Name" {:val "John" :exp 100100}}
```

…not [deftype or defrecord](https://clojure.org/reference/datatypes).

## Consequences
1. The [hash map](https://clojure.org/reference/data_structures#Maps) should perform better than wrapping a map in a [deftype or defrecord](https://clojure.org/reference/datatypes).
1. But the shape of the hash map will become important. For example, with `{"Name" {:val "John"}}`, functions will need to know that the value is at `:val`. If it were wrapped in [deftype](https://clojure.org/reference/datatypes), it could have getters and setters that would hide that detail.
1. It will be harder to change the shape of the hash map.
1. If this repo grows, [deftype or defrecord](https://clojure.org/reference/datatypes) might give better abstraction to the store, if they perform well enough.
1. Clojure [vectors](https://clojure.org/reference/data_structures#Vectors) should perform well for [lists](https://redis.io/docs/data-types/lists/). Getting their [length](https://clojure.org/reference/data_structures#Vectors) is 0(1).
