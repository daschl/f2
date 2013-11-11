/**
 * Copyright (C) 2013 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package com.couchbase.client.core.io.service;

/**
 * Defines the supported {@link ServiceType}s.
 *
 * A {@link ServiceType} just describes the service in a type-safe manner. Every type has a
 * {@link BucketServiceMapping} attached that is used to define how many buckets it can support at the same time.
 */
public enum ServiceType {

    /**
     * View service for Design Documents and Views.
     */
    DESIGN(BucketServiceMapping.ONE_FOR_ALL),

    /**
     * Memcache service for key-based binary ops.
     */
    MEMCACHE(BucketServiceMapping.ONE_BY_ONE);

    /**
     * The strategy to use per type.
     */
    private final BucketServiceMapping mapping;

    /**
     * Create a new {@link ServiceType}.
     *
     * @param mapping the strategy to use.
     */
    ServiceType(BucketServiceMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Returns the {@link BucketServiceMapping} of this {@link ServiceType}.
     *
     * @return the strategy used.
     */
    public BucketServiceMapping mapping() {
        return mapping;
    }
}