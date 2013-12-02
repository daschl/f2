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

package com.couchbase.client.core.io.endpoint.spec;

import com.couchbase.client.core.io.endpoint.Endpoint;
import com.couchbase.client.core.io.endpoint.design.DesignEndpoint;
import com.couchbase.client.core.io.endpoint.memcache.MemcacheEndpoint;
import com.couchbase.client.core.io.service.ServiceType;
import io.netty.channel.EventLoopGroup;
import reactor.core.Environment;
import reactor.function.Supplier;

import java.net.InetSocketAddress;

public class EndpointSpec implements Supplier<Endpoint> {

    private final Environment env;
    private final InetSocketAddress addr;
    private final ServiceType serviceType;
    private EventLoopGroup eventLoopGroup;

    public EndpointSpec(Environment env, InetSocketAddress addr, ServiceType serviceType) {
        this.env = env;
        this.addr = addr;
        this.serviceType = serviceType;
    }

    /**
     * Sets a {@link EventLoopGroup} used for the underlying IO management.
     *
     * @param eventLoopGroup the group to use.
     * @return the {@link EndpointSpec} for proper chaining.
     */
    public EndpointSpec setEventLoopGroup(final EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        return this;
    }

    @Override
    public Endpoint get() {
        if (eventLoopGroup == null) {
            throw new IllegalStateException("A EventLoopGroup must be provided.");
        }

        switch (serviceType) {
            case DESIGN:
                return new DesignEndpoint(addr, env, eventLoopGroup);
            case MEMCACHE:
                return new MemcacheEndpoint(addr, env, eventLoopGroup);
            default:
                throw new IllegalArgumentException("Could not create Endpoint for type: " + serviceType);
        }
    }
}
