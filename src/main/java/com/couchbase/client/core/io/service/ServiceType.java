package com.couchbase.client.core.io.service;

/**
 * Defines the supported {@link ServiceType}s.
 */
public enum ServiceType {

    /**
     * View service for Design Documents and Views.
     */
    DESIGN(BucketServiceStrategy.ONE_FOR_ALL);

    private BucketServiceStrategy strategy;

    ServiceType(BucketServiceStrategy strategy) {
        this.strategy = strategy;
    }

    public BucketServiceStrategy strategy() {
        return strategy;
    }
}
