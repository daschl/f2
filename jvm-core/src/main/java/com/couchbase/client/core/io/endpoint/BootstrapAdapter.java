package com.couchbase.client.core.io.endpoint;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;

/**
 * A adapter that wraps the final {@link Bootstrap} class for easier testing.
 *
 * Technically this class isn't needed, but it is final and does not implement a common interface, so mocking it is
 * hard. Using a wrapper adapter provides a clean solution to the issue without much overhead.
 */
public class BootstrapAdapter {

    private final Bootstrap bootstrap;

    public BootstrapAdapter(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public ChannelFuture connect() {
        return bootstrap.connect();
    }

}
