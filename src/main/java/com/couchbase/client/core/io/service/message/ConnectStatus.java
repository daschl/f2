package com.couchbase.client.core.io.service.message;

/**
 * Identifies the current {@link ConnectStatus} of a {@link com.couchbase.client.core.io.service.Service}.
 */
public class ConnectStatus {

    private final State state;

    public ConnectStatus(State state) {
        this.state = state;
    }

    public State state() {
        return state;
    }

    public static ConnectStatus alreadyConnected() {
        return new ConnectStatus(State.ALREADY_CONNECTED);
    }

    public static ConnectStatus connected() {
        return new ConnectStatus(State.CONNECTED);
    }

    public static ConnectStatus stillConnecting() {
        return new ConnectStatus(State.STILL_CONNECTING);
    }

    public static enum State {
        ALREADY_CONNECTED,
        CONNECTED,
        STILL_CONNECTING
    }
}
