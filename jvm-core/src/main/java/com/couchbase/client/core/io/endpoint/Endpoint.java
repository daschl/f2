package com.couchbase.client.core.io.endpoint;

import reactor.core.composable.Promise;
import reactor.core.composable.Stream;
import reactor.event.Event;

/**
 * A {@link Endpoint} that communicates with the IO layer.
 *
 * The {@link Endpoint} communicates with the underlying transport to read and write data payloads. It is always in a
 * deterministic state that gets changed through external factors (like an instructed connect call or a disconnect from
 * the underlying transport).
 */
public interface Endpoint<REQ, RES> {

    /**
     * Attempt to connect this endpoint.
     *
     * @return
     */
    Promise<EndpointState> connect();

    /**
     * Attempt to disconnect this endpoint.
     *
     * @return
     */
    Promise<EndpointState> disconnect();


    /**
     * Attempt to send the request and receive a response.
     *
     * @param requestEvent the incoming request wrapped in an {@link Event}.
     * @return the deferred response.
     * @throws EndpointNotConnectedException if currently not connected.
     */
    Promise<RES> sendAndReceive(Event<? extends REQ> requestEvent) throws EndpointNotConnectedException;


    /**
     * The current {@link EndpointState}.
     *
     * @return
     */
    EndpointState state();

    /**
     * A {@link Stream} that always gets updated when the {@link EndpointState} changes.
     * @return
     */
    Stream<EndpointState> stateStream();

    /**
     * Helper method to identify if this {@link Endpoint} is connected.
     *
     * @return
     */
    boolean isConnected();

}
