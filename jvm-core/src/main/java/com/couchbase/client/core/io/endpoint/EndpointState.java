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

/**
 * Contains all states in which an {@link Endpoint} can be all the time.
 *
 * A {@link Endpoint} implements a very simple state machine, transitioning through the various connect
 * stages, eventually ending back in the {@link #DISCONNECTED} state after shutdown.
 */
public enum EndpointState {

    /**
     * The {@link Endpoint} is disconnected.
     */
    DISCONNECTED,

    /**
     * The {@link Endpoint} is currently connecting.
     */
    CONNECTING,

    /**
     * The {@link Endpoint} is connected.
     */
    CONNECTED,

    /**
     * The {@link Endpoint} is reconnecting.
     */
    RECONNECTING,

    /**
     * The {@link Endpoint} is disconnecting.
     */
    DISCONNECTING

}
