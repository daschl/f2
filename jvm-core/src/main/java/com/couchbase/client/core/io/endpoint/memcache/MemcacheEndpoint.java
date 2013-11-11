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
import reactor.tcp.netty.NettyClientSocketOptions;

import java.net.InetSocketAddress;

/**
 * Describes an {@link com.couchbase.client.core.io.endpoint.Endpoint} for sending {@link MemcacheRequest}s and
 * receiving {@link MemcacheResponse}s.
 */
public class MemcacheEndpoint extends AbstractEndpoint<MemcacheRequest, MemcacheResponse> {

    private static final NettyClientSocketOptions SOCKET_OPTIONS = new NettyClientSocketOptions()
        .pipelineConfigurer(new Consumer<ChannelPipeline>() {
            @Override
            public void accept(ChannelPipeline pipeline) {
                pipeline
                    .addLast(new BinaryMemcacheClientCodec())
                    .addLast(new BinaryMemcacheObjectAggregator(Integer.MAX_VALUE))
                    .addLast(new MemcacheCodec());
            }
        }
        );

    /**
     * Create a new {@link MemcacheEndpoint} and supply essential params.
     *
     * @param addr the socket address to connect to.
     * @param env the environment to attach to.
     */
    public MemcacheEndpoint(final InetSocketAddress addr, final Environment env) {
        super(addr, env, SOCKET_OPTIONS);
    }

    /**
     * Create a new {@link MemcacheEndpoint} and supply essential params.
     *
     * @param addr the socket address to connect to.
     * @param env the environment to attach to.
     * @param reconnect the {@link reactor.tcp.Reconnect} strategy to use.
     */
    public MemcacheEndpoint(final InetSocketAddress addr, final Environment env, final Reconnect reconnect) {
        super(addr, env, SOCKET_OPTIONS, reconnect);
    }
}
