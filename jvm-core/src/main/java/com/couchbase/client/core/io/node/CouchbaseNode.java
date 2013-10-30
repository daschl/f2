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

package com.couchbase.client.core.io.node;

import com.couchbase.client.core.io.service.BucketServiceStrategy;
import com.couchbase.client.core.io.service.Service;
import com.couchbase.client.core.io.service.ServiceType;
import com.couchbase.client.core.io.service.message.ConnectStatus;
import com.couchbase.client.core.io.service.spec.ServiceSpec;
import com.couchbase.client.core.message.CouchbaseRequest;
import com.couchbase.client.core.message.CouchbaseResponse;
import reactor.core.Environment;
import reactor.core.composable.Promise;
import reactor.core.composable.spec.Promises;
import reactor.event.Event;
import reactor.event.registry.CachingRegistry;
import reactor.event.registry.Registration;
import reactor.event.registry.Registry;
import reactor.event.selector.Selector;
import reactor.function.Function;

import static reactor.event.selector.Selectors.U;


public class CouchbaseNode implements Node {

    private final Registry<Service> registry;
    private final ServiceSpec serviceSpec;

    /**
     * Constructor for proper unit testing of a {@link CouchbaseNode}.
     *
     * @param reg
     * @param spec
     */
    CouchbaseNode(Registry reg, ServiceSpec spec) {
        registry = reg;
        serviceSpec = spec;
    }

    /**
     * Create a new {@link CouchbaseNode}.
     *
     * @param host the hostname.
     * @param env the environment to use.
     */
    public CouchbaseNode(String host, Environment env) {
        registry = new CachingRegistry<Service>();
        serviceSpec = new ServiceSpec().env(env).target(host);
    }

    @Override
    public Promise<ConnectStatus> addService(final ServiceType type, final String bucket) {
        if (hasService(type, bucket)) {
            return Promises.success(ConnectStatus.connected()).get();
        }

        final Service service = serviceSpec.type(type).get();
        Promise<ConnectStatus> connectPromise = service.connect();
        return connectPromise.map(new Function<ConnectStatus, ConnectStatus>() {
            @Override
            public ConnectStatus apply(ConnectStatus connectStatus) {
                registry.register(wildcardSelector(type, bucket), service);
                return connectStatus;
            }
        });
    }

    @Override
    public boolean hasService(ServiceType type, String bucket) {
        validateTypeAndBucket(type.strategy(), bucket);
        return !registry.select(matchSelector(type, bucket)).isEmpty();
    }

    @Override
    public Promise<Void> removeService(ServiceType type, String bucket) {
        if (!hasService(type, bucket)) {
            return Promises.success((Void)null).get();
        }

        Registration<? extends Service> registration = registry.select(matchSelector(type, bucket)).get(0);
        registration.cancel();
        Service service = registration.getObject();
        return service.disconnect();
    }

    @Override
    public Promise<? extends CouchbaseResponse> sendAndReceive(ServiceType type, String bucket,
        Event<? extends CouchbaseRequest> req) {
        validateTypeAndBucket(type.strategy(), bucket);
        if (!hasService(type, bucket)) {
            throw new ServiceNotFoundException("No service of type " + type + " for bucket " + bucket
                + " registered.");
        }
        Registration<? extends Service> registration = registry.select(matchSelector(type, bucket)).get(0);
        return registration.getObject().sendAndReceive(req);
    }

    /**
     * Check if the given strategy and bucket match up for the selector convention.
     *
     * @param strategy the strategy to verify.
     * @param bucket the name of the bucket can be null.
     */
    private void validateTypeAndBucket(final BucketServiceStrategy strategy, final String bucket) {
        boolean isOneByOne = strategy.equals(BucketServiceStrategy.ONE_BY_ONE);
        if ((bucket == null || bucket.isEmpty()) && isOneByOne) {
            throw new IllegalArgumentException("Bucket needs to be provided for strategy: " + strategy);
        }
    }

    /**
     * Selector when attaching a consumer to the reactor.
     *
     * @param type
     * @param bucket
     * @return
     */
    private Selector wildcardSelector(ServiceType type, String bucket) {
        switch (type.strategy()) {
            case ONE_BY_ONE:
                return U("/" + bucket + "/" + type.name());
            case ONE_FOR_ALL:
                return U("/{" + bucket + "}/" + type.name());
            default:
                throw new IllegalArgumentException("Unknown service type" + type);
        }
    }

    /**
     * Selector when locating a consumer in the reactor.
     *
     * @param type
     * @param bucket
     * @return
     */
    private String matchSelector(ServiceType type, String bucket) {
        switch (type.strategy()) {
            case ONE_BY_ONE:
            case ONE_FOR_ALL:
                return "/" + bucket + "/" + type.name();
            default:
                throw new IllegalArgumentException("Unknown service type" + type);
        }
    }


}
