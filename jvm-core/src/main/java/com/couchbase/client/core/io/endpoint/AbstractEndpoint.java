/**
 * Copyright (C) 2013 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package com.couchbase.client.core.io.endpoint;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.composable.Deferred;
import reactor.core.composable.Promise;
import reactor.core.composable.Stream;
import reactor.core.composable.spec.Promises;
import reactor.core.composable.spec.Streams;
import reactor.event.Event;
import reactor.tcp.Reconnect;

import reactor.tcp.spec.IncrementalBackoffReconnectSpec;

import java.net.InetSocketAddress;

/**
 * Implements common functionality needed by all {@link Endpoint}s.
 *
 */
public abstract class AbstractEndpoint<REQ, RES> implements Endpoint<REQ, RES> {

    /**
     * Standard LOGGER to use.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Endpoint.class);

    /**
     * Caching the stateless exception for better runtime performance.
     */
    private static final EndpointNotConnectedException NOT_CONNECTED_EXCEPTION
        = new EndpointNotConnectedException("Endpoint is not connected");

    /**
     * The {@link Environment} to attach to.
     */
    private final Environment env;

    /**
     * Default execution environment to use for promises.
     */
    private final String defaultPromiseEnv =  Environment.THREAD_POOL;

    /**
     * Deferred to get populated when the {@link EndpointState} changes.
     */
    private final Deferred<EndpointState, Stream<EndpointState>> endpointStateDeferred;

    /**
     * Stream of {@link EndpointState} changes.
     */
    private final Stream<EndpointState> endpointStateStream;

    /**
     * {@link Bootstrap} to use for this endpoint.
     */
    private final Bootstrap connectionBootstrap;

    /**
     * Represents the Netty channel of this endpoint.
     */
    private volatile Channel channel;

    /**
     * Holds the current {@link EndpointState}.
     */
    private volatile EndpointState state = EndpointState.DISCONNECTED;

    /**
     * Preload the exceptions to make sure they do not contain misleading values.
     */
    static {
        NOT_CONNECTED_EXCEPTION.setStackTrace(new StackTraceElement[0]);
    }

    /**
     * Create a new {@link AbstractEndpoint} and supply essential params.
     *
     * @param addr the socket address to connect to.
     * @param env the environment to attach to.
     * @param group the {@link EventLoopGroup} to use.
     */
    protected AbstractEndpoint(final InetSocketAddress addr, final Environment env, final EventLoopGroup group) {
        this.env = env;
        endpointStateDeferred = Streams.defer(env, defaultPromiseEnv);
        endpointStateStream = endpointStateDeferred.compose();

        connectionBootstrap = new Bootstrap()
            .group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    //pipeline.addLast(new LoggingHandler());
                    customEndpointHandlers(pipeline);
                    pipeline.addLast(new GenericEndpointHandler<REQ, RES>());
                }
            })
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .remoteAddress(addr);
    }

    /**
     * Add custom endpoint handlers to the {@link ChannelPipeline}.
     *
     * @param pipeline the pipeline where to add handlers.
     */
    protected abstract void customEndpointHandlers(final ChannelPipeline pipeline);

    @Override
    public Promise<EndpointState> connect() {
        if (state == EndpointState.CONNECTED || state  == EndpointState.CONNECTING) {
            return Promises.success(state).get();
        }
        transitionState(EndpointState.CONNECTING);

        final Deferred<EndpointState, Promise<EndpointState>> deferred = Promises.defer(env, defaultPromiseEnv);
        connectionBootstrap.connect().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    channel = future.channel();
                    transitionState(EndpointState.CONNECTED);
                    LOGGER.debug("Successfully connected Endpoint to: " + channel.remoteAddress());
                    deferred.accept(state);
                } else {
                    channel = null;
                    transitionState(EndpointState.DISCONNECTED);
                    LOGGER.debug("Could not connect to Endpoint", future.cause());
                    deferred.accept(state);
                }
            }
        });
        return deferred.compose();
    }

    @Override
    public Promise<EndpointState> disconnect() {
        if (state == EndpointState.DISCONNECTED || state == EndpointState.DISCONNECTING) {
            return Promises.success(state).get();
        }
        transitionState(EndpointState.DISCONNECTING);

        final Deferred<EndpointState, Promise<EndpointState>> deferred = Promises.defer(env, defaultPromiseEnv);
        channel.disconnect().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                transitionState(EndpointState.DISCONNECTED);
                deferred.accept(state);
                if (future.isSuccess()) {
                    LOGGER.debug("Successfully disconnected Endpoint from: " + channel.remoteAddress());
                } else {
                    LOGGER.error("Detected error during Endpoint disconnect phase from: "
                        + channel.remoteAddress(), future.cause());
                }
                channel = null;
            }
        });
        return deferred.compose();
    }

    @Override
    public Promise<RES> sendAndReceive(final Event<? extends REQ> requestEvent) throws EndpointNotConnectedException {
        if (!isConnected()) {
            throw NOT_CONNECTED_EXCEPTION;
        }
        final Deferred<RES, Promise<RES>> deferred = Promises.defer(env, Environment.EVENT_LOOP);
        requestEvent.setReplyTo(deferred);
        channel.write(requestEvent);
        return deferred.compose();
    }

    @Override
    public EndpointState state() {
        return state;
    }

    @Override
    public Stream<EndpointState> stateStream() {
        return endpointStateStream;
    }

    @Override
    public boolean isConnected() {
        return state == EndpointState.CONNECTED;
    }

    /**
     * Transition the {@link EndpointState} and notify the stream.
     *
     * @param newState the new state to apply.
     */
    private void transitionState(final EndpointState newState) {
        if (state != newState) {
            state = newState;
            endpointStateDeferred.accept(newState);
        }
    }
}
