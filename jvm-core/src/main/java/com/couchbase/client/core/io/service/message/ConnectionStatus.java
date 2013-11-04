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

package com.couchbase.client.core.io.service.message;

/**
 * Identifies the current {@link ConnectionStatus} of a {@link com.couchbase.client.core.io.service.Service}.
 */
public class ConnectionStatus {

    private final State state;

    public ConnectionStatus(State state) {
        this.state = state;
    }

    public State state() {
        return state;
    }

    public static ConnectionStatus alreadyConnected() {
        return new ConnectionStatus(State.ALREADY_CONNECTED);
    }

    public static ConnectionStatus connected() {
        return new ConnectionStatus(State.CONNECTED);
    }

    public static ConnectionStatus stillConnecting() {
        return new ConnectionStatus(State.STILL_CONNECTING);
    }

    public static enum State {
        ALREADY_CONNECTED,
        CONNECTED,
        STILL_CONNECTING
    }
}
