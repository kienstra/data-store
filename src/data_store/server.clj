; Props Techniek for most of this file
; https://infi.nl/nieuws/writing-a-tcp-proxy-using-netty-and-clojure/
(ns data-store.server
  (:import
   [io.netty.bootstrap ServerBootstrap]
   [io.netty.channel.socket.nio NioServerSocketChannel]
   [io.netty.channel SimpleChannelInboundHandler
    ChannelInitializer ChannelOption ChannelHandler]
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
      (childOption ChannelOption/AUTO_READ true)
      (childOption ChannelOption/AUTO_CLOSE true)))

(defn server-handler [output-handler store-handler]
  (proxy [SimpleChannelInboundHandler] []
    (channelRead0 [ctx msg]
      (let [input (take-nth
                   2
                   (drop 2 (split (.toString msg (.. StandardCharsets UTF_8)) #"\r\n")))
            [old-store new-store] (swap-vals! store (fn [prev-store]
                                                      (store-handler
                                                       input
                                                       (System/currentTimeMillis)
                                                       prev-store)))]
        (.writeAndFlush (.. ctx channel) (Unpooled/wrappedBuffer (.getBytes (output-handler input (System/currentTimeMillis) old-store new-store))))))
    (exceptionCaught
      [ctx e])))

(defn start-server [port handlers-factory]
  (let [event-loop-group (NioEventLoopGroup.)
        bootstrap (init-server-bootstrap event-loop-group handlers-factory)
        channel (.. bootstrap (bind port) (sync) (channel))]
    channel))

(defn serve! [port output-handler store-handler]
  (start-server
   port
   (fn [] [(server-handler output-handler store-handler)])))
