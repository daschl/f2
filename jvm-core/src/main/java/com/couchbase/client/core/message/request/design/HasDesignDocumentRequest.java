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
