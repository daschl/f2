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

package com.couchbase.client.core.io.endpoint

import io.netty.channel.ChannelPipeline
import io.netty.channel.EventLoopGroup
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.channel.nio.NioEventLoopGroup
import reactor.core.Environment
import reactor.event.Event
import spock.lang.Specification

/**
 * Verifies the functionality of the {@link AbstractEndpoint}.
 */
class AbstractEndpointSpec extends Specification {

    def env = new Environment()
    def embeddedChannel = new EmbeddedChannel()
    def mockedBootstrap = Mock(BootstrapAdapter)
    def endpoint = new DummyEndpoint(mockedBootstrap, env)

    def "A Endpoint should be in a DISCONNECTED state after construction"() {
        expect:
        endpoint.state() == EndpointState.DISCONNECTED
    }

    def "A Endpoint should be in a CONNECTED state after a successful connect attempt"() {
        when:
        def connectPromise = endpoint.connect()

        then:
        1 * mockedBootstrap.connect() >> embeddedChannel.newSucceededFuture()
        connectPromise.await() == EndpointState.CONNECTED
        endpoint.state() == EndpointState.CONNECTED
    }

    def "A Endpoint should be in a RECONNECTING state after a unsuccessful connect attempt"() {
        when:
        def endpoint = new DummyEndpoint(new InetSocketAddress(9988), new Environment(), new NioEventLoopGroup())
        def connectPromise = endpoint.connect()

        then:
        connectPromise.await() == EndpointState.RECONNECTING
        endpoint.state() == EndpointState.RECONNECTING

        when:
        def disconnectPromise = endpoint.disconnect()

        then:
        disconnectPromise.await() == EndpointState.DISCONNECTED
        endpoint.state() == EndpointState.DISCONNECTED
    }

    def "Should silently swallow duplicate CONNECT attempt while still CONNECTING"() {
        setup:
        def promise = embeddedChannel.newPromise()

        when:
        def firstConnectAttempt = endpoint.connect()
        def secondConnectAttempt = endpoint.connect()

        Thread.start {
            sleep(2000)
            promise.setSuccess()
        }

        then:
        1 * mockedBootstrap.connect() >> promise
        firstConnectAttempt.await() == EndpointState.CONNECTED
        secondConnectAttempt.await() == EndpointState.CONNECTING
    }

    def "Should silently swallow duplicate CONNECT attempt when CONNECTED"() {
        when:
        def connectPromise = endpoint.connect()

        then:
        1 * mockedBootstrap.connect() >> embeddedChannel.newSucceededFuture()
        connectPromise.await() == EndpointState.CONNECTED

        when:
        def anotherConnectAttempt = endpoint.connect()

        then:
        0 * mockedBootstrap.connect()
        anotherConnectAttempt.await() == EndpointState.CONNECTED
    }

    def "Should be in a DISCONNECTED state after disconnecting"() {
        when:
        def connectPromise = endpoint.connect()

        then:
        1 * mockedBootstrap.connect() >> embeddedChannel.newSucceededFuture()
        connectPromise.await() == EndpointState.CONNECTED

        when:
        def disconnectPromise = endpoint.disconnect()

        then:
        disconnectPromise.await() == EndpointState.DISCONNECTED
        endpoint.state() == EndpointState.DISCONNECTED
    }

    def "Should throw exception when writing payload and not connected"() {
        when:
        endpoint.sendAndReceive(Event.wrap("Payload"))

        then:
        def e = thrown(EndpointNotConnectedException)
        e.message == "Endpoint is not connected"

    }

    def "Should update the EndpointState Stream during phases"() {
        setup:
        def expectedTransitions = 4
        def stream = endpoint.stateStream()

        when:
        def connectPromise = endpoint.connect().await()
        def disconnectPromise = endpoint.disconnect().await()

        then:
        1 * mockedBootstrap.connect() >> embeddedChannel.newSucceededFuture()
        stream.acceptCount == expectedTransitions
    }

    /**
     * Represents a simple {@link AbstractEndpoint} implementation.
     */
    class DummyEndpoint extends AbstractEndpoint<String, String> {

        DummyEndpoint(InetSocketAddress addr, Environment env, EventLoopGroup group) {
            super(addr, env, group)
        }

        DummyEndpoint(BootstrapAdapter bootstrap, Environment env) {
            super(bootstrap, env)
        }

        @Override
        protected void customEndpointHandlers(ChannelPipeline pipeline) {

        }
    }
}