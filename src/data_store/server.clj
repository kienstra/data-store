; Props Techniek for most of this file
; https://infi.nl/nieuws/writing-a-tcp-proxy-using-netty-and-clojure/
(ns data-store.server
  (:import
   [io.netty.bootstrap ServerBootstrap Bootstrap]
   [io.netty.channel.socket.nio NioServerSocketChannel NioSocketChannel]
   [io.netty.channel ChannelInboundHandlerAdapter
    ChannelInitializer ChannelOption ChannelHandler ChannelFutureListener
    ChannelOutboundHandlerAdapter]
   [io.netty.channel.nio NioEventLoopGroup]
   [io.netty.handler.codec ByteToMessageDecoder]
   [io.netty.handler.logging LoggingHandler LogLevel]
   [io.netty.buffer Unpooled]
   [java.nio ByteBuffer ByteOrder]
   [java.io ByteArrayOutputStream]
   [io.netty.handler.codec.bytes ByteArrayEncoder]
   [java.util.concurrent LinkedBlockingQueue Executors]
   [java.util ArrayList]
   [java.time Instant])
  (:require [clojure.string :refer [split]]
            [clojure.pprint :refer [pprint]]
            [data-store.store :refer [store]]))

(declare flush-and-close)

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
      (childOption ChannelOption/AUTO_READ false)
      (childOption ChannelOption/AUTO_CLOSE false)))

(defn server-handler []
  (let [outgoing-channel (atom nil)]
    (proxy [ChannelInboundHandlerAdapter] []
      (channelActive [ctx]
        (->
         (.. ctx channel read)))
      (channelRead [ctx msg]
        (->
         (.writeAndFlush ctx msg)
         (.addListener
          (proxy [ChannelFutureListener] []
            (operationComplete [complete-future]
              (if (.isSuccess complete-future)
                (.. ctx channel read)
                (flush-and-close (.. ctx channel))))))))
      (channelInactive [ctx]
        (when @outgoing-channel
          (flush-and-close @outgoing-channel)))
      (exceptionCaught
       [ctx e]
       (pprint e)
       (.close (.. ctx channel))))))

(defn start-server [port handlers-factory]
  (let [event-loop-group (NioEventLoopGroup.)
        bootstrap (init-server-bootstrap event-loop-group handlers-factory)
        channel (.. bootstrap (bind port) (sync) (channel))]

    channel))

(defn flush-and-close [channel]
  (->
   (.writeAndFlush channel Unpooled/EMPTY_BUFFER)
   (.addListener ChannelFutureListener/CLOSE)))

(defn print-netty-inbound-handler []
  (proxy [ChannelInboundHandlerAdapter] []
    (channelRead [ctx msg]
      (pprint msg)
      (.fireChannelRead ctx msg))))

(defn serve! [port _]
  (start-server
   port
   (fn []
     [(LoggingHandler. "proxy" LogLevel/INFO)
      (print-netty-inbound-handler)
      (server-handler)])))
