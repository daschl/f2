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

package com.couchbase.client.core.io.service.memcache;

import com.couchbase.client.core.io.service.Service;
import com.couchbase.client.core.io.service.message.ConnectionStatus;
import com.couchbase.client.core.message.request.memcache.MemcacheRequest;
import com.couchbase.client.core.message.response.memcache.MemcacheResponse;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.memcache.binary.BinaryMemcacheClientCodec;
import io.netty.handler.codec.memcache.binary.BinaryMemcacheObjectAggregator;
import reactor.core.Environment;
import reactor.core.composable.Deferred;
import reactor.core.composable.Promise;
import reactor.core.composable.spec.Promises;
import reactor.event.Event;
import reactor.function.Consumer;
import reactor.tcp.TcpClient;
import reactor.tcp.TcpConnection;
import reactor.tcp.netty.NettyClientSocketOptions;
import reactor.tcp.netty.NettyTcpClient;
import reactor.tcp.spec.TcpClientSpec;

import java.net.InetSocketAddress;


public class MemcacheService implements Service<MemcacheRequest, MemcacheResponse> {

    public static final int DEFAULT_PORT = 11210;

    private final TcpClient<MemcacheResponse, MemcacheRequest> client;
    private final Environment environment;

    private volatile TcpConnection<MemcacheResponse, MemcacheRequest> connection;
    private volatile boolean connecting;

    /**
     * Constructor used for testing the {@link MemcacheService}.
     *
     * @param client a custom client to use.
     * @param env the environment.
     */
    MemcacheService(final TcpClient<MemcacheResponse, MemcacheRequest> client, final Environment env) {
        this.client = client;
        environment = env;
        connecting = false;
    }

    /**
     * Create a new {@link MemcacheService}.
     *
     * @param addr the socket to connect to.
     * @param env the environment to use.
     */
    public MemcacheService(final InetSocketAddress addr, final Environment env) {
        environment = env;
        client = new TcpClientSpec<MemcacheResponse, MemcacheRequest>(NettyTcpClient.class)
            .env(env)
            .options(new NettyClientSocketOptions().pipelineConfigurer(new Consumer<ChannelPipeline>() {
                @Override
                public void accept(ChannelPipeline pipeline) {
                    pipeline
                        .addLast(new BinaryMemcacheClientCodec())
                        .addLast(new BinaryMemcacheObjectAggregator(Integer.MAX_VALUE))
                        .addLast(new MemcacheCodec());
                }
            }))
            .connect(addr)
            .get();
    }

    @Override
    public Promise<ConnectionStatus> connect() {
        if (isConnected()) {
            return Promises.success(ConnectionStatus.alreadyConnected()).get();
        }
        if (connecting) {
            return Promises.success(ConnectionStatus.stillConnecting()).get();
        }

        final Deferred<ConnectionStatus, Promise<ConnectionStatus>> connectStatus =
            Promises.defer(environment, Environment.THREAD_POOL);

        connecting = true;
        client.open().onComplete(new Consumer<Promise<TcpConnection<MemcacheResponse, MemcacheRequest>>>() {
            @Override
            public void accept(Promise<TcpConnection<MemcacheResponse, MemcacheRequest>> promise) {
                if (promise.isSuccess()) {
                    connection = promise.get();
                    connectStatus.accept(ConnectionStatus.connected());
                } else {
                    connectStatus.accept(promise.reason());
                }
                connecting = false;
            }
        });

        return connectStatus.compose();
    }

    @Override
    public Promise<Void> disconnect() {
        return client.close();
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

    @Override
    public Promise<MemcacheResponse> sendAndReceive(Event<MemcacheRequest> event) {
        return connection.sendAndReceive(event.getData());
    }
}
