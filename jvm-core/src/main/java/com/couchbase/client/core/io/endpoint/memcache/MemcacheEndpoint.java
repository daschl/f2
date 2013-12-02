package com.couchbase.client.core.io.endpoint.memcache;

import com.couchbase.client.core.io.endpoint.AbstractEndpoint;
import com.couchbase.client.core.message.request.memcache.MemcacheRequest;
import com.couchbase.client.core.message.response.memcache.MemcacheResponse;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.memcache.binary.BinaryMemcacheClientCodec;
import io.netty.handler.codec.memcache.binary.BinaryMemcacheObjectAggregator;
import reactor.core.Environment;
import reactor.function.Consumer;
import reactor.tcp.Reconnect;
import reactor.tcp.TcpClient;
import reactor.tcp.netty.NettyClientSocketOptions;

import java.net.InetSocketAddress;

/**
 * Describes an {@link com.couchbase.client.core.io.endpoint.Endpoint} for sending {@link MemcacheRequest}s and
 * receiving {@link MemcacheResponse}s.
 */
public class MemcacheEndpoint extends AbstractEndpoint<MemcacheRequest, MemcacheResponse> {

    public MemcacheEndpoint(final InetSocketAddress addr, final Environment env) {
        super(addr, env);
    }

    @Override
    protected void customEndpointHandlers(final ChannelPipeline pipeline) {
        pipeline
            .addLast(new BinaryMemcacheClientCodec())
            .addLast(new BinaryMemcacheObjectAggregator(Integer.MAX_VALUE))
            .addLast(new MemcacheCodec());
    }

}
