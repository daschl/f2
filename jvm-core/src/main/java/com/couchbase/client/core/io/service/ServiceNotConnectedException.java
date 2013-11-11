package com.couchbase.client.core.io.service;

/**
 * Identifies that the contacted {@link Service} is not connected.
 */
public class ServiceNotConnectedException extends IllegalStateException {

    public ServiceNotConnectedException(String s) {
        super(s);
    }

}
