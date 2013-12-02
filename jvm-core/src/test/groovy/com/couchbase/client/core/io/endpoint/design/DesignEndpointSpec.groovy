package com.couchbase.client.core.io.endpoint.design

import com.couchbase.client.core.io.endpoint.EndpointState
import com.couchbase.client.core.message.request.design.GetDesignDocumentRequest
import com.couchbase.client.core.message.request.memcache.GetRequest
import com.couchbase.client.core.message.response.memcache.MemcacheResponse
import reactor.core.Environment
import reactor.core.composable.Promise
import reactor.event.Event
import spock.lang.Specification

/**
 * Verifies the correct functionality of the {@link DesignEndpoint}
 */
class DesignEndpointSpec extends Specification {

    def env = new Environment()

    def "A DesignEndpoint should accept a DesignRequest and respond with a DesignResponse"() {
        setup:
        DesignEndpoint endpoint = new DesignEndpoint(new InetSocketAddress(8092), env)

        when:
        Promise<EndpointState> connectPromise = endpoint.connect()

        then:
        connectPromise.await() == EndpointState.CONNECTED

        when:
        Promise<MemcacheResponse> response = endpoint.sendAndReceive(Event.wrap(new GetDesignDocumentRequest("default", "foo")));

        then:
        System.out.println(response.await())

    }


    def "ruuuun"() {
        when:
        DesignEndpoint endpoint = new DesignEndpoint(new InetSocketAddress(8092), env)
        endpoint.connect().await()

        then:
        while (true) {
            endpoint.sendAndReceive(Event.wrap(new GetDesignDocumentRequest("default", "foo"))).await()

        }
    }

}
