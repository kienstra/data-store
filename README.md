# Data Store

A lightweight in-memory data store in [Clojure](https://clojure.org/).

Similar to Redis®* but fewer features.

### Run server locally

1. [Install Leiningen](https://leiningen.org/) if you haven't already
1. Install the [Redis CLI](https://redis.io/docs/ui/cli/)
1. `lein run`
1. Open another terminal tab
1. `redis-cli PING`
1. Expected: `PONG`
1. Run any other [Redis CLI command](https://redis.io/docs/ui/cli/)

### Run unit tests

`lein test`

### Package server for production

`lein uberjar`

### Run server in production

`java -jar target/uberjar/data-store-<version>-standalone.jar`

### Benchmark server

1. [Run server](#run-server-in-production)
2. Open a new terminal tab
3. `redis-benchmark -t set,get, -n 1000000 -q`
4. Expected:
```sh
WARNING: Could not fetch server CONFIG
SET: 72270.00 requests per second, p50=0.343 msec
GET: 74833.49 requests per second, p50=0.351 msec
```
5. Stop the server you ran in step 1 with Control-C.
6. `redis-server`
7. Repeat step 3
8. Expected:
```sh
SET: 80729.80 requests per second, p50=0.303 msec
GET: 81512.88 requests per second, p50=0.319 msec
```
9. This gives a comparison of how this server compares to Redis®.

### Bugs

Please open an [issue](https://github.com/kienstra/data-store/issues) on GitHub.

### License

[GPLv2](LICENSE) or later

---

\* Redis is a registered trademark of Redis Ltd. Any rights therein are reserved to Redis Ltd. Any use by this repo is for referential purposes only and does not indicate any sponsorship, endorsement or affiliation between Redis and this repo.
