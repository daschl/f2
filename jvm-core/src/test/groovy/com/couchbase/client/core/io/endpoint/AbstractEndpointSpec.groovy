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
import io.netty.channel.nio.NioEventLoopGroup
import reactor.core.Environment

import spock.lang.Specification

/**
 * Verifies the functionality of the {@link AbstractEndpoint}.
 */
class AbstractEndpointSpec extends Specification {

    def env = new Environment()
    def addr = new InetSocketAddress(1234)
    def group = new NioEventLoopGroup()
    def notReconnectingEndpoint = new DummyEndpoint(addr, env, group)

    def "A Endpoint should be in a DISCONNECTED state after construction"() {
        expect:
        notReconnectingEndpoint.state() == EndpointState.DISCONNECTED
    }

    def "A Endpoint should be in a CONNECTED state after a successful connect attempt"() {
    }

    def "A Endpoint should be in a DISCONNECTED state after a unsuccessful connect attempt"() {
    }

    def "Should silently swallow duplicate CONNECT attempt while still CONNECTING"() {
    }

    def "Should silently swallow duplicate CONNECT attempt when CONNECTED"() {
    }

    def "Should be in a DISCONNECTED state after disconnecting"() {
    }

    def "Should swallow disconnect attempt when DISCONNECTED"() {
    }

    def "Should swallow disconnect attempt when DISCONNECTING"() {
    }

    def "Should throw exception when writing payload and not connected"() {
    }

    def "Should update the EndpointState Stream during phases"() {
    }

    /**
     * Represents a simple {@link AbstractEndpoint} implementation.
     */
    class DummyEndpoint extends AbstractEndpoint<String, String> {

        DummyEndpoint(InetSocketAddress addr, Environment env, EventLoopGroup group) {
            super(addr, env, group)
        }

        @Override
        protected void customEndpointHandlers(ChannelPipeline pipeline) {

        }
    }
}