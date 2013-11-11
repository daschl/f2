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

package com.couchbase.client.core.io.endpoint.design;

import com.couchbase.client.core.io.endpoint.AbstractEndpoint;
import com.couchbase.client.core.message.request.design.DesignRequest;
import com.couchbase.client.core.message.response.design.DesignResponse;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import reactor.core.Environment;
import reactor.function.Consumer;
import reactor.tcp.Reconnect;
import reactor.tcp.netty.NettyClientSocketOptions;

import java.net.InetSocketAddress;

/**
 * Describes an {@link com.couchbase.client.core.io.endpoint.Endpoint} for sending {@link DesignRequest}s and
 * receiving {@link DesignResponse}s.
 */
public class DesignEndpoint extends AbstractEndpoint<DesignRequest, DesignResponse> {

    private static final NettyClientSocketOptions SOCKET_OPTIONS = new NettyClientSocketOptions()
        .pipelineConfigurer(new Consumer<ChannelPipeline>() {
            @Override
            public void accept(ChannelPipeline pipeline) {
                pipeline
                    .addLast(new HttpClientCodec())
                    .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                    .addLast(new DesignCodec());
            }
        }
    );

    /**
     * Create a new {@link DesignEndpoint} and supply essential params.
     *
     * @param addr the socket address to connect to.
     * @param env the environment to attach to.
     */
    public DesignEndpoint(final InetSocketAddress addr, final Environment env) {
        super(addr, env, SOCKET_OPTIONS);
    }

    /**
     * Create a new {@link DesignEndpoint} and supply essential params.
     *
     * @param addr the socket address to connect to.
     * @param env the environment to attach to.
     * @param reconnect the {@link reactor.tcp.Reconnect} strategy to use.
     */
    public DesignEndpoint(final InetSocketAddress addr, final Environment env, final Reconnect reconnect) {
        super(addr, env, SOCKET_OPTIONS, reconnect);
    }
}
