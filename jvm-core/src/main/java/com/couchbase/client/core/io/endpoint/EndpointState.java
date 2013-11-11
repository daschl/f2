package com.couchbase.client.core.io.endpoint;

/**
 * Contains all states in which an {@link Endpoint} can be all the time.
 */
public enum EndpointState {

    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    DISCONNECTING

}
