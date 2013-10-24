package com.couchbase.client.core.message.request.design;

/**
 * Check to see if the design document exists on the cluster.
 */
public class HasDesignDocumentRequest implements DesignRequest {

    private final String bucket;
    private final String design;
    private final String user;
    private final String password;

    public HasDesignDocumentRequest(String bucket, String design) {
        this(bucket, design, null, null);
    }

    public HasDesignDocumentRequest(String bucket, String design, String user, String password) {
        this.bucket = bucket;
        this.design = design;
        this.user = user;
        this.password = password;
    }

    @Override
    public String path() {
        return "/" + bucket + "/_design/" + design;
    }

    @Override
    public String user() {
        return user;
    }

    @Override
    public String password() {
        return password;
    }
}
