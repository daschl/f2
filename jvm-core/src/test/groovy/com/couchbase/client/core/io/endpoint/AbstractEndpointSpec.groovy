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
import reactor.core.Environment
import reactor.core.composable.Promise
import reactor.core.composable.Stream
import reactor.core.composable.spec.Streams
import reactor.core.composable.spec.Promises
import reactor.event.Event
import reactor.tcp.Reconnect
import reactor.tcp.TcpClient
import reactor.tcp.TcpConnection
import reactor.tcp.spec.IncrementalBackoffReconnectSpec
import spock.lang.Specification

/**
 * Verifies the functionality of the {@link AbstractEndpoint}.
 */
class AbstractEndpointSpec extends Specification {

    def env = new Environment()
    def TcpClient mockedClient = Mock(TcpClient)
    def notReconnectingEndpoint = new DummyEndpoint(mockedClient, env, 1)
    def connectDeferred = Streams.<TcpConnection>defer(env)
    def disconnectDeferred = Promises.<Void>defer(env)

    def "A Endpoint should be in a DISCONNECTED state after construction"() {
        expect:
        notReconnectingEndpoint.state() == EndpointState.DISCONNECTED
    }

    def "A Endpoint should be in a CONNECTED state after a successful connect attempt"() {
        when:
        Promise<EndpointState> connectPromise = notReconnectingEndpoint.connect()
        connectDeferred.accept(Mock(TcpConnection))

        then:
        1 * mockedClient.open(_ as Reconnect) >> connectDeferred.compose()
        connectPromise.await() == EndpointState.CONNECTED
        notReconnectingEndpoint.state() == EndpointState.CONNECTED
    }

    def "A Endpoint should be in a DISCONNECTED state after a unsuccessful connect attempt"() {
        when:
        Promise<EndpointState> connectPromise = notReconnectingEndpoint.connect()
        connectDeferred.accept(new Exception("Something bad happened!"))

        then:
        1 * mockedClient.open(_ as Reconnect) >> connectDeferred.compose()
        connectPromise.await() == EndpointState.DISCONNECTED
        notReconnectingEndpoint.state() == EndpointState.DISCONNECTED
    }

    def "Should silently swallow duplicate CONNECT attempt while still CONNECTING"() {
        when:
        Promise<EndpointState> firstAttempt = notReconnectingEndpoint.connect()
        Promise<EndpointState> secondAttempt = notReconnectingEndpoint.connect()
        Thread.start {
            sleep(100)
            connectDeferred.accept(Mock(TcpConnection))
        }

        then:
        1 * mockedClient.open(_ as Reconnect) >> connectDeferred.compose()
        secondAttempt.await() == EndpointState.CONNECTING
        firstAttempt.await() == EndpointState.CONNECTED
    }

    def "Should silently swallow duplicate CONNECT attempt when CONNECTED"() {
        when:
        Promise<EndpointState> firstAttempt = notReconnectingEndpoint.connect()
        connectDeferred.accept(Mock(TcpConnection))

        then:
        1 * mockedClient.open(_ as Reconnect) >> connectDeferred.compose()
        firstAttempt.await() == EndpointState.CONNECTED

        when:
        Promise<EndpointState> secondAttempt = notReconnectingEndpoint.connect()

        then:
        0 * mockedClient.open(_ as Reconnect) >> connectDeferred.compose()
        secondAttempt.await() == EndpointState.CONNECTED
    }

    def "Should be in a DISCONNECTED state after disconnecting"() {
        when:
        Promise<EndpointState> connectState = notReconnectingEndpoint.connect()
        connectDeferred.accept(Mock(TcpConnection))

        then:
        1 * mockedClient.open(_ as Reconnect) >> connectDeferred.compose()
        connectState.await() == EndpointState.CONNECTED

        when:
        Promise<EndpointState> disconnectState = notReconnectingEndpoint.disconnect()
        disconnectDeferred.accept((Void) null)

        then:
        1 * mockedClient.close() >> disconnectDeferred.compose()
        disconnectState.await() == EndpointState.DISCONNECTED
    }

    def "Should swallow disconnect attempt when DISCONNECTED"() {
        when:
        EndpointState disconnectState = notReconnectingEndpoint.disconnect().await()

        then:
        0 * mockedClient.close()
        disconnectState == EndpointState.DISCONNECTED
    }

    def "Should swallow disconnect attempt when DISCONNECTING"() {
        when:
        Promise<EndpointState> connectState = notReconnectingEndpoint.connect()
        connectDeferred.accept(Mock(TcpConnection))

        then:
        1 * mockedClient.open(_ as Reconnect) >> connectDeferred.compose()
        connectState.await() == EndpointState.CONNECTED

        when:
        Promise<EndpointState> firstAttempt = notReconnectingEndpoint.disconnect()
        Promise<EndpointState> secondAttempt = notReconnectingEndpoint.disconnect()
        disconnectDeferred.accept((Void) null)

        then:
        1 * mockedClient.close() >> disconnectDeferred.compose()
        secondAttempt.await() == EndpointState.DISCONNECTING
        firstAttempt.await()  == EndpointState.DISCONNECTED
    }

    def "Should throw exception when writing payload and not connected"() {
        when:
        notReconnectingEndpoint.sendAndReceive(Event.wrap("Hello World"))

        then:
        thrown(EndpointNotConnectedException)
        notReconnectingEndpoint.state() == EndpointState.DISCONNECTED
    }

    def "Should update the EndpointState Stream during phases"() {
        setup:
        def expectedTransitions = 4

        when:
        Stream<EndpointState> stream = notReconnectingEndpoint.<EndpointState>stateStream()
        Promise<EndpointState> connectPromise = notReconnectingEndpoint.connect()
        connectDeferred.accept(Mock(TcpConnection))
        Promise<EndpointState> disconnectPromise = notReconnectingEndpoint.disconnect()
        disconnectDeferred.accept((Void) null)

        then:
        with(mockedClient) {
            1 * open(_ as Reconnect) >> connectDeferred.compose()
            1 * close() >> disconnectDeferred.compose()
        }
        stream.acceptCount == expectedTransitions
    }

    /**
     * Represents a simple {@link AbstractEndpoint} implementation.
     */
    class DummyEndpoint extends AbstractEndpoint<String, String> {

        DummyEndpoint(InetSocketAddress addr, Environment env) {
            super(addr, env)
        }

        @Override
        protected void customEndpointHandlers(ChannelPipeline pipeline) {

        }
    }
}