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

package com.couchbase.client.core.io.node.spec;

import com.couchbase.client.core.io.node.CouchbaseNode;
import com.couchbase.client.core.io.node.Node;
import reactor.core.Environment;
import reactor.function.Supplier;
import reactor.util.Assert;

/**
 * Factory class to easily create {@link com.couchbase.client.core.io.node.Node} instances.
 */
public class NodeSpec implements Supplier<Node> {

    private Environment env;
    private String host;

    /**
     * The {@link Environment} to use.
     *
     * @param env the environment to use.
     * @return the {@link NodeSpec} to allow for chaining.
     */
    public NodeSpec env(Environment env) {
        this.env = env;
        return this;
    }

    /**
     * The hostname/IP of the remote server.
     *
     * @param host the hostname/ip.
     * @return the {@link NodeSpec} to allow for chaining.
     */
    public NodeSpec host(String host) {
        this.host = host;
        return this;
    }

    @Override
    public Node get() {
        Assert.notNull(host, "A target host needs to be specified.");
        Assert.notNull(env, "A Environment needs to be specified.");

        return new CouchbaseNode(host, env);
    }

}
