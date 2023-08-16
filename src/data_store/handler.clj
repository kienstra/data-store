(ns data-store.handler)

(defn handler [store input]
  (if-let [command (nth input 2 false)]
    (cond
      (= command "PING")
      [store "$4\r\nPONG\r\n"]
      (= command "ECHO")
      (if (nth input 4 false)
        [store (str (nth input 3) "\r\n" (nth input 4) "\r\n")]
        [store "-Error nothing to echo\r\n"])
      (= command "SET")
      (if
       (nth input 6 false)
        [(into store {(nth input 4) (nth input 6)}) "$2\r\nOK\r\n"]
        [store "-Error nothing to set\r\n"])
      (= command "GET")
      (if
       (nth input 4 false)
        [store (if (nil? (get store (nth input 4)))
                 "-Error key not found\r\n"
                 (str "$" (count (get store (nth input 4))) "\r\n" (get store (nth input 4)) "\r\n"))]
        [store "-Error nothing to get\r\n"])
      :else [store "-Error invalid command\r\n"])
    [store "-Error no command \r\n"]))
