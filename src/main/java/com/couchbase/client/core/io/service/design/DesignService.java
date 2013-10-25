package com.couchbase.client.core.io.service.design;

import com.couchbase.client.core.io.service.BucketServiceStrategy;
import com.couchbase.client.core.io.service.Service;
import com.couchbase.client.core.io.service.ServiceType;
import com.couchbase.client.core.io.service.message.ConnectStatus;
import com.couchbase.client.core.message.request.design.DesignRequest;
import com.couchbase.client.core.message.response.design.DesignResponse;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;
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

/**
 * A {@link com.couchbase.client.core.io.service.Service} dealing with View and DesignDocument messages.
 */
public class DesignService implements Service<DesignRequest, DesignResponse> {

    public static final int DEFAULT_PORT = 8092;

    private final TcpClient<DesignResponse, DesignRequest> client;
    private final Environment environment;

    private volatile TcpConnection<DesignResponse, DesignRequest> connection;
    private volatile boolean connecting;


    /**
     * Constructor used for testing the {@link DesignService}.
     *
     * @param client a custom client to use.
     * @param env the environment.
     */
    DesignService(final TcpClient<DesignResponse, DesignRequest> client, final Environment env) {
        this.client = client;
        environment = env;
        connecting = false;
    }

    /**
     * Create a new {@link DesignService}.
     *
     * @param addr the socket to connect to.
     * @param env the environment to use.
     */
    public DesignService(final InetSocketAddress addr, final Environment env) {
        environment = env;
        client = new TcpClientSpec<DesignResponse, DesignRequest>(NettyTcpClient.class)
            .env(env)
            .options(new NettyClientSocketOptions().pipelineConfigurer(new Consumer<ChannelPipeline>() {
                @Override
                public void accept(ChannelPipeline pipeline) {
                    pipeline
                        .addLast(new HttpClientCodec())
                        .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                        .addLast(new DesignCodec());
                }
            }))
            .connect(addr)
            .get();
    }

    @Override
    public Promise<ConnectStatus> connect() {
        if (isConnected()) {
            return Promises.success(ConnectStatus.alreadyConnected()).get();
        }
        if (connecting) {
            return Promises.success(ConnectStatus.stillConnecting()).get();
        }

        final Deferred<ConnectStatus, Promise<ConnectStatus>> connectStatus =
            Promises.defer(environment, Environment.THREAD_POOL);

        connecting = true;
        client.open().onComplete(new Consumer<Promise<TcpConnection<DesignResponse, DesignRequest>>>() {
            @Override
            public void accept(Promise<TcpConnection<DesignResponse, DesignRequest>> promise) {
                if (promise.isSuccess()) {
                    connection = promise.get();
                    connectStatus.accept(ConnectStatus.connected());
                } else {
                    connectStatus.accept(promise.reason());
                }
                connecting = false;
            }
        });

        return connectStatus.compose();
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

    @Override
    public Promise<DesignResponse> sendAndReceive(Event<DesignRequest> event) {
        return connection.sendAndReceive(event.getData());
    }

    @Override
    public Promise<Void> disconnect() {
        return client.close();
    }

}
