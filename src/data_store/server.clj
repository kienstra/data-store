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

(defn server-handler [output-handler store-handler]
  (proxy [SimpleChannelInboundHandler] []
    (channelActive [ctx]
      (.. ctx channel read))
    (channelInactive [ctx]
      (flush-and-close (.. ctx channel)))
    (channelRead0 [ctx msg]
      ; store-handler
      ; output-handler
      ; If there's a store-handler, run swap-values! with that
      ; Run output-handler on the new-store and previous-store
      ; There should always be an output handler
      ; So the swap-values! function should be pure
      (let [input (take-nth
                   2
                   (rest
                    (rest (split (.toString msg (.. StandardCharsets UTF_8)) #"\r\n"))))]
        (if store-handler
          (let [[old-store new-store] (swap-vals! store (fn [prev-store]
                                                          (store-handler
                                                           prev-store
                                                           input
                                                           (System/currentTimeMillis))))]
            (.writeAndFlush (.. ctx channel) (Unpooled/wrappedBuffer (.getBytes (output-handler new-store input (System/currentTimeMillis))))))
          (.writeAndFlush (.. ctx channel) (Unpooled/wrappedBuffer (.getBytes (output-handler @store input (System/currentTimeMillis))))))))
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
