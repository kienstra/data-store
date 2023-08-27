; Props Techniek for most of this file
; https://infi.nl/nieuws/writing-a-tcp-proxy-using-netty-and-clojure/
(ns data-store.server
  (:import
   [io.netty.bootstrap ServerBootstrap]
   [io.netty.channel.socket.nio NioServerSocketChannel]
   [io.netty.channel SimpleChannelInboundHandler
    ChannelInitializer ChannelOption ChannelHandler
    ChannelFutureListener]
   [io.netty.channel.nio NioEventLoopGroup]
   [io.netty.buffer Unpooled]
   [java.nio.charset StandardCharsets])
  (:require [clojure.string :refer [split]]
            [data-store.store :refer [store]]))

(defn init-server-bootstrap
  [group handlers-factory]
  (.. (ServerBootstrap.)
      (group group)
      (channel NioServerSocketChannel)
      (childHandler
       (proxy [ChannelInitializer] []
         (initChannel [channel]
           (let [handlers (handlers-factory)]
             (.. channel
                 (pipeline)
                 (addLast (into-array ChannelHandler handlers)))))))
      (childOption ChannelOption/SO_KEEPALIVE true)
      (childOption ChannelOption/AUTO_READ true)
      (childOption ChannelOption/AUTO_CLOSE true)))

(defn flush-and-close [channel]
  (->
   (.writeAndFlush channel Unpooled/EMPTY_BUFFER)
   (.addListener ChannelFutureListener/CLOSE)))

(defn server-handler [handler]
  (proxy [SimpleChannelInboundHandler] []
    (channelActive [ctx]
      (.. ctx channel read))
    (channelInactive [ctx]
      (flush-and-close (.channel ctx)))
    (channelRead0 [ctx msg]
      (swap! store (fn [prev-store]
                     (let [[new-store out] (handler
                                            prev-store
                                            (take-nth
                                             2
                                             (rest
                                              (rest (split (.toString msg (.. StandardCharsets UTF_8)) #"\r\n"))))
                                            (System/currentTimeMillis))]
                       (.writeAndFlush (.. ctx channel) (Unpooled/wrappedBuffer (.getBytes out)))
                       new-store))))
    (exceptionCaught
      [ctx e])))

(defn start-server [port handlers-factory]
  (let [event-loop-group (NioEventLoopGroup.)
        bootstrap (init-server-bootstrap event-loop-group handlers-factory)
        channel (.. bootstrap (bind port) (sync) (channel))]
    channel))

(defn serve! [port handler]
  (start-server
   port
   (fn [] [(server-handler handler)])))
