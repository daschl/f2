package com.couchbase.client.core.io.endpoint.memcache

import com.couchbase.client.core.io.endpoint.EndpointState
import com.couchbase.client.core.io.endpoint.design.DesignEndpoint
import com.couchbase.client.core.message.request.design.GetDesignDocumentRequest
import com.couchbase.client.core.message.request.memcache.GetRequest
import com.couchbase.client.core.message.response.memcache.GetResponse
import com.couchbase.client.core.message.response.memcache.MemcacheResponse
import reactor.core.Environment
import reactor.core.composable.Promise
import reactor.event.Event
import reactor.function.Consumer
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

/**
 * Verifies the correct functionality of the {@link MemcacheEndpoint}
 */
class MemcacheEndpointSpec extends Specification {

    def env = new Environment()

    def "A MemcacheEndpoint should accept a MemcacheRequest and respond with a MemcacheResponse"() {
        setup:
        MemcacheEndpoint endpoint = new MemcacheEndpoint(new InetSocketAddress(11210), env)

        when:
        Promise<EndpointState> connectPromise = endpoint.connect()

        then:
        connectPromise.await() == EndpointState.CONNECTED

        when:
        Promise<MemcacheResponse> dataPromise = endpoint.sendAndReceive(Event.wrap(new GetRequest("foo")));

        then:
        dataPromise.await() instanceof GetResponse
    }

    def "ruuuun"() {
        when:
        MemcacheEndpoint endpoint = new MemcacheEndpoint(new InetSocketAddress(8092), env)
        endpoint.connect().await()

        then:
        while (true) {
            endpoint.sendAndReceive(Event.wrap(new GetRequest("default"))).await()

        }
    }


}
