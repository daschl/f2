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

import com.couchbase.client.core.io.service.message.ConnectStatus;
import com.couchbase.client.core.message.CouchbaseRequest;
import com.couchbase.client.core.message.CouchbaseResponse;
import reactor.core.composable.Promise;
import reactor.event.Event;

/**
 * Defines the common methods for all {@link Service} implementations.
 *
 * Every {@link Service} needs to implement a response that follows a request.
 */
public interface Service<REQ extends CouchbaseRequest, RES extends CouchbaseResponse> {

    /**
     * Connect the {@link Service} to the endpoint.
     *
     * @return a {@link Promise} containing the status.
     */
    Promise<ConnectStatus> connect();

    /**
     * Disconnect the {@link Service} from the endpoint.
     *
     * @return a {@link Promise} when disconnect is finished.
     */
    Promise<Void> disconnect();

    /**
     * Returns true if the service is currently connected.
     *
     * @return true if the service is connected.
     */
    boolean isConnected();

    /**
     * Werite a Request and receive a response from the service.
     *
     * @param event
     * @return
     */
    Promise<RES> sendAndReceive(Event<REQ> event);


}
