package com.couchbase.client.core.message.request.design;

import com.couchbase.client.core.message.CouchbaseRequest;

/**
 * Defines a {@link CouchbaseRequest} to be used against a
 * {@link com.couchbase.client.core.io.service.design.DesignService}.
 */
public interface DesignRequest extends CouchbaseRequest {

    /**
     * The path needed for the request.
     *
     * @return the relative path.
     */
    String path();

    /**
     * The username needed for the request.
     *
     * @return the username.
     */
    String user();

    /**
     * The password needed for the request.
     *
     * @return the password.
     */
    String password();
}
