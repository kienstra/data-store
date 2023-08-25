; Props Artem Yankov for most of this file
; https://github.com/yankov/memobot/blob/9498f02d1f32c3133c67581f31e959557e05048f/src/memobot/core.clj
(ns data-store.server
  (:import
   [io.netty.bootstrap ServerBootstrap]
   [io.netty.channel ChannelOption ChannelInitializer ChannelFutureListener]
   [io.netty.channel.nio NioEventLoopGroup]
   [io.netty.channel ChannelInboundHandlerAdapter]
   [io.netty.channel.socket.nio NioServerSocketChannel]
   [io.netty.buffer Unpooled])
  (:require [clojure.string :refer [split]]
            [data-store.store :refer [store]]))

(defn serve!
  [port handler]
  (let [event-loop-group (NioEventLoopGroup.)
        channel NioServerSocketChannel
        bootstrap (.. (ServerBootstrap.)
                                  (group event-loop-group)
                                  (channel channel)
                                  (childHandler
                                   (proxy [ChannelInboundHandlerAdapter] []
                                     (channelActive [ctx]
                                                    (.. ctx channel read))
                                     (channelInactive [ctx]
                                                      (->
                                                       (.writeAndFlush channel Unpooled/EMPTY_BUFFER)
                                                       (.addListener ChannelFutureListener/CLOSE)))
                                     (channelRead [ctx e]
                                                      (let [c (.getChannel e)
                                                            cb (.getMessage e)
                                                            msg (.toString cb "UTF-8")]
                                                        (swap! store (fn [prev-store]
                                                                       (let [[new-store out] (handler
                                                                                              prev-store
                                                                                              (take-nth
                                                                                               2
                                                                                               (rest
                                                                                                (rest (split msg #"\r\n"))))
                                                                                              (System/currentTimeMillis))]
                                                                         (.write c (Unpooled/wrappedBuffer (.getBytes out)))
                                                                         new-store)))))
                                     (exceptionCaught
                                      [ctx e]
                                      (-> e .getChannel .close))))
                                  (childOption ChannelOption/SO_KEEPALIVE true)
                                  (childOption ChannelOption/AUTO_READ false)
                                  (childOption ChannelOption/AUTO_CLOSE false))
       channel (.. bootstrap (bind port) (sync) (channel))]
    (-> channel
        .closeFuture
        (.addListener
         (proxy [ChannelFutureListener] []
           (operationComplete [fut]
             (.shutdownGracefully event-loop-group)))))))
