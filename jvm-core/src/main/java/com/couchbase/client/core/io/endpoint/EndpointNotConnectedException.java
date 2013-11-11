package com.couchbase.client.core.io.endpoint;

/**
 * Identifies that the contacted {@link Endpoint} is not connected.
 */
public class EndpointNotConnectedException extends IllegalStateException {

    public EndpointNotConnectedException(String s) {
        super(s);
    }

}
