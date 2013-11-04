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

import com.couchbase.client.core.io.service.ServiceType;
import com.couchbase.client.core.io.service.message.ConnectionStatus;
import com.couchbase.client.core.message.CouchbaseRequest;
import com.couchbase.client.core.message.CouchbaseResponse;
import reactor.core.composable.Promise;
import reactor.event.Event;

/**
 * Represents a Couchbase Node that handles one or more {@link com.couchbase.client.core.io.service.Service}s.
 */
public interface Node {

    /**
     * Add a {@link com.couchbase.client.core.io.service.Service} to the {@link Node}.
     *
     * @param type
     * @param bucket
     * @return
     */
    Promise<ConnectionStatus> addService(final ServiceType type, final String bucket);

    /**
     * Check if the given {@link com.couchbase.client.core.io.service.Service} is registered.
     *
     * @param type
     * @param bucket
     * @return
     */
    boolean hasService(final ServiceType type, final String bucket);

    /**
     * Remove the {@link com.couchbase.client.core.io.service.Service} fromt he {@link Node}.
     *
     * @param type
     * @param bucket
     * @return
     */
    Promise<Void> removeService(final ServiceType type, final String bucket);

    /**
     * Write and read from an attached service.
     *
     * @param type
     * @param bucket
     * @param req
     * @return
     */
    Promise<? extends CouchbaseResponse> sendAndReceive(ServiceType type, String bucket, Event<? extends CouchbaseRequest> req);
}
