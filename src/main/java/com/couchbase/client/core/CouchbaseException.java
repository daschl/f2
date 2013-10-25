package com.couchbase.client.core;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 25/10/13
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public class CouchbaseException extends RuntimeException {

    public CouchbaseException() {
    }

    public CouchbaseException(String message) {
        super(message);
    }

    public CouchbaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CouchbaseException(Throwable cause) {
        super(cause);
    }

    public CouchbaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
