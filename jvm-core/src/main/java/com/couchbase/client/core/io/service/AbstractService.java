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

package com.couchbase.client.core.io.service;

import com.couchbase.client.core.io.endpoint.Endpoint;
import com.couchbase.client.core.io.endpoint.spec.EndpointSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.composable.Deferred;
import reactor.core.composable.Promise;
import reactor.core.composable.Stream;
import reactor.core.composable.spec.Streams;
import reactor.event.Event;
import reactor.event.registry.CachingRegistry;
import reactor.event.registry.Registration;
import reactor.event.registry.Registry;

import java.net.InetSocketAddress;
import java.util.Iterator;

import static reactor.event.selector.Selectors.$;

/**
 * Base class that implements all tasks needed by {@link Service} implementations.
 */
public abstract class AbstractService<REQ, RES> implements Service<REQ, RES> {

    /**
     * Standard LOGGER to use.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

    /**
     * Caching the stateless exception for better runtime performance.
     */
    private static final ServiceNotConnectedException NOT_CONNECTED_EXCEPTION
        = new ServiceNotConnectedException("Service is not connected");

    /**
     * Default execution pool to use for promises.
     */
    private final String defaultPool =  Environment.THREAD_POOL;

    /**
     * The {@link Environment} to attach to.
     */
    private final Environment env;

    /**
     * Deferred to get populated when the {@link ServiceState} changes.
     */
    private final Deferred<ServiceState, Stream<ServiceState>> serviceStateDeferred;

    /**
     * Stream of {@link ServiceState} changes.
     */
    private final Stream<ServiceState> serviceStateStream;

    /**
     * Holds all currently registered {@link Endpoint}s.
     */
    private final Registry<Endpoint<REQ, RES>> endpointRegistry;

    /**
     * Holds the current {@link ServiceState}.
     */
    private volatile ServiceState state = ServiceState.DISCONNECTED;

    /**
     * Preload the exceptions to make sure they do not contain misleading values.
     */
    static {
        NOT_CONNECTED_EXCEPTION.setStackTrace(new StackTraceElement[0]);
    }

    /**
     * Defines the {@link ServiceType} to use for this {@link Service}.
     *
     * @return the used {@link ServiceType}.
     */
    protected abstract ServiceType serviceType();

    /**
     * Constructor helpful for testing and mocking the registry.
     *
     * @param registry the registry with all the endpoints.
     * @param env the environment to use.
     */
    AbstractService(Registry<Endpoint<REQ, RES>> registry, Environment env) {
        this.env = env;
        endpointRegistry = registry;

        serviceStateDeferred = Streams.defer(env, defaultPool);
        serviceStateStream = serviceStateDeferred.compose();
    }

    /**
     * Create a new {@link AbstractService} and initialize the underlying {@link Endpoint}s.
     *
     * @param remoteAddress the remote address to use.
     * @param endpointPoolSize the amount of {@link Endpoint} objects to manage.
     * @param env the environment to use.
     */
    protected AbstractService(final InetSocketAddress remoteAddress, short endpointPoolSize, Environment env) {
        this.env = env;
        endpointRegistry = new CachingRegistry<Endpoint<REQ, RES>>();

        ServiceType serviceType = serviceType();

        EndpointSpec spec = new EndpointSpec(env, remoteAddress, serviceType);
        for (int i = 0; i < endpointPoolSize; i++) {
            endpointRegistry.register($("/" + serviceType + "/" + i), spec.get());
        }

        serviceStateDeferred = Streams.defer(env, defaultPool);
        serviceStateStream = serviceStateDeferred.compose();
    }

    @Override
    public Promise<ServiceState> connect() {
        Iterator<Registration<? extends Endpoint<REQ, RES>>> iter = endpointRegistry.iterator();
        while (iter.hasNext()) {
            Registration<? extends Endpoint<REQ, RES>> registration = iter.next();
            Endpoint endpoint = registration.getObject();
            endpoint.connect();
        }
        return null;
    }

    @Override
    public Promise<ServiceState> disconnect() {
        Iterator<Registration<? extends Endpoint<REQ, RES>>> iter = endpointRegistry.iterator();
        while (iter.hasNext()) {
            Registration<? extends Endpoint<REQ, RES>> registration = iter.next();
            Endpoint endpoint = registration.getObject();
            endpoint.disconnect();
        }
        return null;
    }

    @Override
    public Promise<RES> sendAndReceive(Event<REQ> requestEvent) throws ServiceNotConnectedException {
        return null;
    }

    @Override
    public ServiceState state() {
        return state;
    }

    @Override
    public Stream<ServiceState> stateStream() {
        return serviceStateStream;
    }

    @Override
    public boolean isConnected() {
        return state == ServiceState.CONNECTED;
    }

    /**
     * Transition the {@link ServiceState} and notify the stream.
     *
     * @param newState the new state to apply.
     */
    private void transitionState(final ServiceState newState) {
        if (state != newState) {
            state = newState;
            serviceStateDeferred.accept(newState);
        }
    }
}
