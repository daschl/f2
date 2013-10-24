package com.couchbase.client.core.message.response.design;

import com.couchbase.client.core.message.CouchbaseResponse;

/**
 * A {@link CouchbaseResponse} coming from a {@link com.couchbase.client.core.io.service.design.DesignService}.
 */
public interface DesignResponse extends CouchbaseResponse {

    /**
     * Returns the status of the response.
     *
     * @return the status of the response.
     */
    Status status();

    /**
     * Contains all possible states of a {@link DesignResponse}.
     */
    static enum Status {
        /**
         * Maps to a HTTP 20* response.
         */
        FOUND,

        /**
         * Maps to a HTTP 30* response.
         */
        REDIRECT,

        /**
         * Maps to a HTTP 401 response.
         */
        UNAUTHORIZED,

        /**
         * Maps to a HTTP 404 response.
         */
        NOT_FOUND,

        /**
         * Maps to a HTTP 50* response.
         */
        SERVER_ERROR
    }
}
