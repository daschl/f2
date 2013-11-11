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

import reactor.core.composable.Promise;
import reactor.core.composable.Stream;
import reactor.event.Event;

/**
 * A {@link Service} consists of one or more {@link com.couchbase.client.core.io.endpoint.Endpoint}s to provide
 * and multiplex the underlying IO operations.
 */
public interface Service<REQ, RES> {

    /**
     * Attempt to connect this endpoint.
     *
     * @return
     */
    Promise<ServiceState> connect();

    /**
     * Attempt to disconnect this endpoint.
     *
     * @return
     */
    Promise<ServiceState> disconnect();


    /**
     * Attempt to send the request and receive a response.
     *
     * @param requestEvent the incoming request wrapped in an {@link reactor.event.Event}.
     * @return the deferred response.
     * @throws ServiceNotConnectedException if currently not connected.
     */
    Promise<RES> sendAndReceive(Event<REQ> requestEvent) throws ServiceNotConnectedException;


    /**
     * The current {@link ServiceState}.
     *
     * @return
     */
    ServiceState state();

    /**
     * A {@link Stream} that always gets updated when the {@link ServiceState} changes.
     *
     * @return
     */
    Stream<ServiceState> stateStream();

    /**
     * Helper method to identify if this {@link Service} is connected.
     *
     * @return
     */
    boolean isConnected();
}
