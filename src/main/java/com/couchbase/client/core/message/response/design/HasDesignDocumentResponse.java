package com.couchbase.client.core.message.response.design;

/**
 *
 */
public class HasDesignDocumentResponse implements DesignResponse {

    private final Status status;

    public HasDesignDocumentResponse(final Status status) {
        this.status = status;
    }

    @Override
    public Status status() {
        return status;
    }

}
