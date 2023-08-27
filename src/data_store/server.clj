; Props Techniek for most of this file
; https://infi.nl/nieuws/writing-a-tcp-proxy-using-netty-and-clojure/
(ns data-store.server
  (:import
   [io.netty.bootstrap ServerBootstrap]
   [io.netty.channel.socket.nio NioServerSocketChannel]
   [io.netty.channel ChannelInboundHandlerAdapter
    ChannelInitializer ChannelOption ChannelHandler ChannelFutureListener]
   [io.netty.channel.nio NioEventLoopGroup]
   [io.netty.buffer Unpooled]
   [java.nio.charset StandardCharsets])
  (:require [clojure.string :refer [split]]
            [data-store.store :refer [store]]))

(declare flush-and-close)

(defn init-server-bootstrap
  [group handlers]
  (.. (ServerBootstrap.)
      (group group)
      (channel NioServerSocketChannel)
      (childHandler
       (proxy [ChannelInitializer] []
         (initChannel [channel]
                      (.. channel
                          (pipeline)
                          (addLast (into-array ChannelHandler handlers))))))
      (childOption ChannelOption/SO_KEEPALIVE true)
      (childOption ChannelOption/AUTO_READ false)
      (childOption ChannelOption/AUTO_CLOSE false)))

(defn server-handler [handler]
  (let [outgoing-channel (atom nil)]
    (proxy [ChannelInboundHandlerAdapter] []
      (channelActive [ctx]
        (->
         (.. ctx channel read)))
      (channelRead [ctx msg]
        (swap! store (fn [prev-store]
                       (let [[new-store out] (handler
                                              prev-store
                                              (take-nth
                                               2
                                               (rest
                                                (rest (split (.toString msg (.. StandardCharsets UTF_8)) #"\r\n"))))
                                              (System/currentTimeMillis))]
                         (.writeAndFlush ctx (Unpooled/wrappedBuffer (.getBytes out)))
                         new-store))))
      (exceptionCaught
       [ctx e]
       ))))

(defn start-server [port handlers]
  (let [event-loop-group (NioEventLoopGroup.)
        bootstrap (init-server-bootstrap event-loop-group handlers)
        channel (.. bootstrap (bind port) (sync) (channel))]

    channel))

(defn flush-and-close [channel]
  (->
   (.writeAndFlush channel Unpooled/EMPTY_BUFFER)
   (.addListener ChannelFutureListener/CLOSE)))

(defn serve! [port handler]
  (start-server
   port
   [(server-handler handler)]))
