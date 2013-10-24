package com.couchbase.client.core.message.response.design;


public class GetDesignDocumentResponse implements DesignResponse {

    private final Status status;
    private final String content;

    public GetDesignDocumentResponse(Status status, String content) {
        this.status = status;
        this.content = content;
    }

    @Override
    public Status status() {
        return status;
    }

    public String content() {
        return content;
    }
}
