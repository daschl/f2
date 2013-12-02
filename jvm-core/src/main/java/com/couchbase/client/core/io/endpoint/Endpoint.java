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

package com.couchbase.client.core.io.endpoint;

import reactor.core.composable.Promise;
import reactor.core.composable.Stream;
import reactor.event.Event;

/**
 * A {@link Endpoint} that communicates with the IO layer.
 *
 * The {@link Endpoint} communicates with the underlying transport to read and write data payloads. It is always in a
 * deterministic state that gets changed through external factors (like an instructed connect call or a disconnect from
 * the underlying transport).
 */
public interface Endpoint<REQ, RES> {

    /**
     * Attempt to connect this endpoint.
     *
     * @return
     */
    Promise<EndpointState> connect();

    /**
     * Attempt to disconnect this endpoint.
     *
     * @return
     */
    Promise<EndpointState> disconnect();


    /**
     * Attempt to send the request and receive a response.
     *
     * @param requestEvent the incoming request wrapped in an {@link Event}.
     * @return the deferred response.
     * @throws EndpointNotConnectedException if currently not connected.
     */
    Promise<RES> sendAndReceive(Event<? extends REQ> requestEvent) throws EndpointNotConnectedException;


    /**
     * The current {@link EndpointState}.
     *
     * @return
     */
    EndpointState state();

    /**
     * A {@link Stream} that always gets updated when the {@link EndpointState} changes.
     * @return
     */
    Stream<EndpointState> stateStream();

    /**
     * Helper method to identify if this {@link Endpoint} is connected.
     *
     * @return
     */
    boolean isConnected();

}
