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
