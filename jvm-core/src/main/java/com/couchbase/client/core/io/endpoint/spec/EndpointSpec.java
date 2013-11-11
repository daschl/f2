package com.couchbase.client.core.io.endpoint.spec;

import com.couchbase.client.core.io.endpoint.AbstractEndpoint;
import com.couchbase.client.core.io.endpoint.Endpoint;
import com.couchbase.client.core.io.endpoint.design.DesignEndpoint;
import com.couchbase.client.core.io.endpoint.memcache.MemcacheEndpoint;
import com.couchbase.client.core.io.service.ServiceType;
import reactor.core.Environment;
import reactor.function.Supplier;
import reactor.tcp.Reconnect;

import java.net.InetSocketAddress;


public class EndpointSpec implements Supplier<Endpoint> {

    private final Environment env;
    private final InetSocketAddress addr;
    private final ServiceType serviceType;
    private Reconnect reconnectStrategy;

    public EndpointSpec(Environment env, InetSocketAddress addr, ServiceType serviceType) {
        this.env = env;
        this.addr = addr;
        this.serviceType = serviceType;
        this.reconnectStrategy = AbstractEndpoint.DEFAULT_RECONNECT_STRATEGY;
    }

    /**
     * Override the default {@link Reconnect} strategy.
     *
     * @param reconnect the strategy to apply.
     * @return the {@link EndpointSpec} for object chaining.
     */
    public EndpointSpec reconnectStrategy(Reconnect reconnect) {
        this.reconnectStrategy = reconnect;
        return this;
    }

    @Override
    public Endpoint get() {
        switch (serviceType) {
            case DESIGN:
                return new DesignEndpoint(addr, env, reconnectStrategy);
            case MEMCACHE:
                return new MemcacheEndpoint(addr, env, reconnectStrategy);
            default:
                throw new IllegalArgumentException("Could not create Endpoint for type: " + serviceType);
        }
    }
}
