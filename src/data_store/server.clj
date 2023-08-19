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
    (channelConnected [ctx e]
      (let [c (.getChannel e)]
        (println "Connected:" c)))

    (channelDisconnected [ctx e]
      (let [c (.getChannel e)]
        (println "Disconnected:" c)))
    (messageReceived [ctx e]
      (let [c (.getChannel e)
            cb (.getMessage e)
            msg (.toString cb "UTF-8")]
        (.write c (ChannelBuffers/copiedBuffer (.getBytes (second (handler {} msg)))))))

    (exceptionCaught
      [ctx e]
      (let [throwable (.getCause e)]
        (println "@exceptionCaught" throwable))
      (-> e .getChannel .close))))
