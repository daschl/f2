package com.couchbase.client.core.io.node;

import com.couchbase.client.core.io.service.ServiceType;
import com.couchbase.client.core.io.service.message.ConnectStatus;
import reactor.core.composable.Promise;

/**
 * Represents a Couchbase Node that handles one or more {@link com.couchbase.client.core.io.service.Service}s.
 */
public interface Node {

    /**
     * Add a {@link com.couchbase.client.core.io.service.Service} to the {@link Node}.
     *
     * @param type
     * @param bucket
     * @return
     */
    Promise<ConnectStatus> addService(final ServiceType type, final String bucket);

    /**
     * Check if the given {@link com.couchbase.client.core.io.service.Service} is registered.
     *
     * @param type
     * @param bucket
     * @return
     */
    boolean hasService(final ServiceType type, final String bucket);

    /**
     * Remove the {@link com.couchbase.client.core.io.service.Service} fromt he {@link Node}.
     *
     * @param type
     * @param bucket
     * @return
     */
    Promise<Void> removeService(final ServiceType type, final String bucket);
}
