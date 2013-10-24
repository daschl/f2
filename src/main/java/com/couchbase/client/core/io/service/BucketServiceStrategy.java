package com.couchbase.client.core.io.service;

/**
 * Defines the a bucket handling strategy for each service.
 */
public enum BucketServiceStrategy {

    /**
     * The Service can handle all buckets at once.
     */
    ONE_FOR_ALL,

    /**
     * Every bucket needs its own service.
     */
    ONE_BY_ONE
}
