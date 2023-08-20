(ns data-store.handler)

(def delim "\r\n")
(defn handler [store input time]
  (let [command (nth input 2)]
    (cond
      (nil? command)
      [store (str "-Error no command" delim)]
      (= command "PING")
      [store (str "$4" delim "PONG" delim)]
      (= command "ECHO")
      (if (nth input 4 false)
        [store (str "+" (nth input 4) delim)]
        [store (str "-Error nothing to echo" delim)])
      (= command "SET")
      (cond
        (not (nth input 6 nil))
        [store (str "-Error nothing to set" delim)]
        (not (string? (nth input 6 nil)))
        [store (str "-Error not a string" delim)]
        :else (let [sub-command (nth input 7 nil)
                    sub-command-arg (nth input 8 nil)]
                (if (and (= sub-command "EX") sub-command-arg)
                  [(into store {(nth input 4) {:val (nth input 6) :exp (+ time (* 1000 sub-command-arg))}}) (str "+OK" delim)]
                  [(into store {(nth input 4) {:val (nth input 6)}}) (str "+OK" delim)])))
      (= command "GET")
      (cond
        (not (nth input 4 nil))
        [store (str "-Error nothing to get" delim)]
        (not (contains? store (nth input 4)))
        [store (str "$-1" delim)]
        (not (string? (:val (get store (nth input 4 nil)))))
        [store (str "-Error not a string" delim)]
        :else (let [exp (:exp (get store (nth input 4)))
                    expired? (and exp (> time exp))]
                (if
                 expired?
                  [store (str "$-1" delim)]
                  [store (str "+" (:val (get store (nth input 4))) delim)])))
      :else [store (str "-Error invalid command" delim)])))
