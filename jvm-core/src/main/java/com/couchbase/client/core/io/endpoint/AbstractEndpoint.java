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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.composable.Deferred;
import reactor.core.composable.Promise;
import reactor.core.composable.Stream;
import reactor.core.composable.spec.Promises;
import reactor.core.composable.spec.Streams;
import reactor.event.Event;
import reactor.function.Consumer;
import reactor.tcp.Reconnect;
import reactor.tcp.TcpClient;
import reactor.tcp.TcpConnection;
import reactor.tcp.netty.NettyClientSocketOptions;
import reactor.tcp.netty.NettyTcpClient;
import reactor.tcp.spec.IncrementalBackoffReconnectSpec;
import reactor.tcp.spec.TcpClientSpec;

import java.net.InetSocketAddress;

/**
 * Implements common functionality needed by all {@link Endpoint}s.
 *
 * TODO:
 *  - test reconnect logic
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
     * The default reconnect strategy ("only try once") if no other is passed in explicitly.
     */
    public static final Reconnect DEFAULT_RECONNECT_STRATEGY =
        new IncrementalBackoffReconnectSpec().maxAttempts(1).get();

    /**
     * The {@link Environment} to attach to.
     */
    private final Environment env;

    /**
     * Default execution pool to use for promises.
     */
    private final String defaultPool =  Environment.THREAD_POOL;

    /**
     * Contains the {@link TcpClient} from where the {@link Endpoint} connects to a {@link TcpConnection}.
     */
    private final TcpClient<RES, REQ> client;

    /**
     * Deferred to get populated when the {@link EndpointState} changes.
     */
    private final Deferred<EndpointState, Stream<EndpointState>> endpointStateDeferred;

    /**
     * Stream of {@link EndpointState} changes.
     */
    private final Stream<EndpointState> endpointStateStream;

    /**
     * Contains the {@link Reconnect} strategy for this endpoint.
     */
    private final Reconnect reconnectStrategy;

    /**
     * The (potentially bound) {@link TcpConnection}.
     */
    private volatile TcpConnection<RES, REQ> connection;

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
     * Create a new {@link Endpoint} by passing in the {@link TcpClient} explicitly.
     *
     * Since no explicit {@link Reconnect} strategy is used, no reconnect logic is applied (one connect try and if not,
     * fail).
     *
     * @param client a potentially mocked {@link TcpClient}.
     * @param env the environment to attach to.
     */
    protected AbstractEndpoint(final TcpClient<RES, REQ> client, final Environment env) {
        this(client, env, DEFAULT_RECONNECT_STRATEGY);
    }

    /**
     * Create a new {@link Endpoint} by passing in the {@link TcpClient} explicitly.
     *
     * @param client a potentially mocked {@link TcpClient}.
     * @param env the environment to attach to.
     * @param reconnect the {@link Reconnect} strategy to use.
     */
    protected AbstractEndpoint(final TcpClient<RES, REQ> client, final Environment env, final Reconnect reconnect) {
        this.env = env;
        this.client = client;
        this.reconnectStrategy = reconnect;
        endpointStateDeferred = Streams.defer(env, defaultPool);
        endpointStateStream = endpointStateDeferred.compose();
    }

    /**
     * Create a new {@link AbstractEndpoint} and supply essential params.
     *
     * @param addr the socket address to connect to.
     * @param env the environment to attach to.
     * @param opts options for the channel pipeline.
     */
    protected AbstractEndpoint(final InetSocketAddress addr, final Environment env,
        final NettyClientSocketOptions opts) {
        this(
            new TcpClientSpec<RES, REQ>(NettyTcpClient.class).env(env).options(opts).connect(addr).get(),
            env,
            DEFAULT_RECONNECT_STRATEGY
        );
    }

    /**
     * Create a new {@link AbstractEndpoint} and supply essential params.
     *
     * @param addr the socket address to connect to.
     * @param env the environment to attach to.
     * @param opts options for the channel pipeline.
     * @param reconnect the {@link Reconnect} strategy to use.
     */
    protected AbstractEndpoint(final InetSocketAddress addr, final Environment env, final NettyClientSocketOptions opts,
        final Reconnect reconnect) {
        this(
            new TcpClientSpec<RES, REQ>(NettyTcpClient.class).env(env).options(opts).connect(addr).get(),
            env,
            reconnect
        );
    }

    @Override
    public Promise<EndpointState> connect() {
        if (state == EndpointState.CONNECTED || state  == EndpointState.CONNECTING) {
            return Promises.success(state).get();
        }
        transitionState(EndpointState.CONNECTING);

        final Deferred<EndpointState, Promise<EndpointState>> deferred = Promises.defer(env, defaultPool);
        client.open(reconnectStrategy)
            .consume(new Consumer<TcpConnection<RES, REQ>>() {
                @Override
                public void accept(TcpConnection<RES, REQ> conn) {
                    connection = conn;
                    transitionState(EndpointState.CONNECTED);
                    LOGGER.debug("Successfully connected Endpoint to: " + conn.remoteAddress());
                    deferred.accept(state);
                }
            })
            .when(Exception.class, new Consumer<Exception>() {
                @Override
                public void accept(Exception e) {
                    connection = null;
                    transitionState(EndpointState.DISCONNECTED);
                    LOGGER.debug("Could not connect to Endpoint", e);
                    deferred.accept(state);
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

        final Deferred<EndpointState, Promise<EndpointState>> deferred = Promises.defer(env, defaultPool);
        client.close().onComplete(new Consumer<Promise<Void>>() {
            @Override
            public void accept(Promise<Void> promise) {
                transitionState(EndpointState.DISCONNECTED);
                deferred.accept(state);
                if (promise.isSuccess()) {
                    LOGGER.debug("Successfully disconnected Endpoint from: " + connection.remoteAddress());
                } else {
                    LOGGER.error("Detected error during Endpoint disconnect phase from: "
                        + connection.remoteAddress(), promise.reason());
                }
                connection = null;
            }
        });
        return deferred.compose();
    }

    @Override
    public Promise<RES> sendAndReceive(final Event<REQ> requestEvent) throws EndpointNotConnectedException {
        if (!isConnected()) {
            throw NOT_CONNECTED_EXCEPTION;
        }
        return connection.sendAndReceive(requestEvent.getData());
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
