package com.couchbase.client.core.io.service;

/**
 * Contains all states in which an {@link Service} can be all the time.
 */
public enum ServiceState {

    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    DISCONNECTING

}
