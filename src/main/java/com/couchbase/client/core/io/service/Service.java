package com.couchbase.client.core.io.service;

import com.couchbase.client.core.io.service.message.ConnectStatus;
import com.couchbase.client.core.message.CouchbaseRequest;
import com.couchbase.client.core.message.CouchbaseResponse;
import com.couchbase.client.core.message.request.design.DesignRequest;
import com.couchbase.client.core.message.response.design.DesignResponse;
import reactor.core.composable.Promise;
import reactor.event.Event;
import reactor.function.Consumer;
import reactor.function.Function;

/**
 * Defines the common methods for all {@link Service} implementations.
 *
 * Every {@link Service} needs to implement a response that follows a request.
 */
public interface Service<REQ extends CouchbaseRequest, RES extends CouchbaseResponse> {

    /**
     * Connect the {@link Service} to the endpoint.
     *
     * @return a {@link Promise} containing the status.
     */
    Promise<ConnectStatus> connect();

    /**
     * Disconnect the {@link Service} from the endpoint.
     *
     * @return a {@link Promise} when disconnect is finished.
     */
    Promise<Void> disconnect();

    /**
     * Returns true if the service is currently connected.
     *
     * @return true if the service is connected.
     */
    boolean isConnected();


    /**
     * Werite a Request and receive a response from the service.
     *
     * @param event
     * @return
     */
    Promise<RES> sendAndReceive(Event<REQ> event);


}
