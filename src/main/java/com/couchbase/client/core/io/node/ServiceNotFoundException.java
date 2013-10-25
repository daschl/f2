package com.couchbase.client.core.io.node;

import com.couchbase.client.core.CouchbaseException;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 25/10/13
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public class ServiceNotFoundException extends CouchbaseException {

    public ServiceNotFoundException() {
    }

    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotFoundException(Throwable cause) {
        super(cause);
    }

    public ServiceNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
