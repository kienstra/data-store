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

### Bugs

Please open an [issue](https://github.com/kienstra/data-store/issues) on GitHub.

### License

[GPLv2](LICENSE) or later

---

\* Redis is a registered trademark of Redis Ltd. Any rights therein are reserved to Redis Ltd. Any use by this repo is for referential purposes only and does not indicate any sponsorship, endorsement or affiliation between Redis and this repo.
