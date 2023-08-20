; Props Artem Yankov for most of this file
; https://github.com/yankov/memobot/blob/9498f02d1f32c3133c67581f31e959557e05048f/src/memobot/core.clj
(ns data-store.server
  (:import
   [java.net InetSocketAddress]
   [java.util.concurrent Executors]
   [org.jboss.netty.bootstrap ServerBootstrap]
   [org.jboss.netty.channel SimpleChannelHandler]
   [org.jboss.netty.channel.socket.nio NioServerSocketChannelFactory]
   [org.jboss.netty.buffer ChannelBuffers]))

(declare make-handler)
(def store (atom {}))

(defn serve
  [port handler]
  (let [channel-factory (NioServerSocketChannelFactory.
                         (Executors/newCachedThreadPool)
                         (Executors/newCachedThreadPool))
        bootstrap (ServerBootstrap. channel-factory)
        pipeline (.getPipeline bootstrap)]
    (.addLast pipeline "handler" (make-handler handler))
    (.setOption bootstrap "child.tcpNoDelay", true)
    (.setOption bootstrap "child.keepAlive", true)
    (.bind bootstrap (InetSocketAddress. port))
    pipeline))

(defn make-handler [handler]
  (proxy [SimpleChannelHandler] []
    (messageReceived [ctx e]
      (let [c (.getChannel e)
            cb (.getMessage e)
            msg (.toString cb "UTF-8")]
        (swap! store (fn [prev-store]
                       (let [[new-store out] (handler prev-store msg)]
                         (.write c (ChannelBuffers/copiedBuffer (.getBytes out)))
                         new-store)))))
    (exceptionCaught
     [ctx e]
     (-> e .getChannel .close))))
