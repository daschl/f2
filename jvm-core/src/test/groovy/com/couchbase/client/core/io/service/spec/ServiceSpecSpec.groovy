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

package com.couchbase.client.core.io.service.spec

import com.couchbase.client.core.io.service.Service
import com.couchbase.client.core.io.service.ServiceType
import com.couchbase.client.core.io.service.design.DesignService
import com.couchbase.client.core.io.service.memcache.MemcacheService
import reactor.core.Environment
import spock.lang.Specification

/**
 * Verifies the functionality of the SUT {@link ServiceSpec}.
 */
class ServiceSpecSpec extends Specification {

    static final String targetHost = "127.0.0.1"
    static final int targetPort = 8092

    def env = new Environment()
    def spec = new ServiceSpec()

    def "A ServiceSpec needs to be constructed with all required options"() {
        when: "initialized without a ServiceType"
            spec.get()
        then: "throws an exception"
            def e = thrown(IllegalArgumentException)
            e.message == "A ServiceType needs to be specified."

        when: "initialized without a target host"
            spec.type(ServiceType.DESIGN).get()
        then: "throws an exception"
            e = thrown(IllegalArgumentException)
            e.message == "A target host needs to be specified."

        when: "initialized with a port that is zero"
            spec.type(ServiceType.DESIGN).target(targetHost, 0).get()
        then: "throws an exception"
            e = thrown(IllegalArgumentException)
            e.message == "Port needs to be greater than zero."

        when: "initialized with a port that is less than zero"
            spec.type(ServiceType.DESIGN).target(targetHost, -1).get()
        then: "throws an exception"
            e = thrown(IllegalArgumentException)
            e.message == "Port needs to be greater than zero."

        when: "initialized without an environment"
            spec.type(ServiceType.DESIGN).target(targetHost).get()
        then: "throws an exception"
            e = thrown(IllegalArgumentException)
            e.message == "A Environment needs to be specified."

        when: "initialized without an environment"
            spec.type(ServiceType.DESIGN).target(targetHost, targetPort).get()
        then: "throws an exception"
            e = thrown(IllegalArgumentException)
            e.message == "A Environment needs to be specified."

        when: "initialized with all properties and default port"
            Service s = spec.type(ServiceType.DESIGN).target(targetHost).env(env).get()
        then: "provides a instantiated service"
            notThrown(IllegalArgumentException)
            s instanceof Service
            !s.connected

        when: "initialized with all properties and custom port"
            s = spec.type(ServiceType.DESIGN).target(targetHost, targetPort).env(env).get()
        then: "provides a instantiated service"
            notThrown(IllegalArgumentException)
            s instanceof Service
            !s.connected
    }

    def "A ServiceSpec is able to construct a DesignService"() {
        when: "initialized with a ServiceType, target host and Environment"
            Service s = spec.type(ServiceType.DESIGN).target(targetHost).env(env).get()
        then: "it should be instantiated but not connected"
            s instanceof DesignService
            !s.connected

        when: "initialized with a ServiceType, target host, custom port and Environment"
            s = spec.type(ServiceType.DESIGN).target(targetHost, targetPort).env(env).get()
        then: "it should be instantiated but not connected"
            s instanceof DesignService
            !s.connected
    }

    def "A ServiceSpec is able to construct a MemcacheService"() {
        when: "initialized with a ServiceType, target host and Environment"
            Service s = spec.type(ServiceType.MEMCACHE).target(targetHost).env(env).get()
        then: "it should be instantiated but not connected"
            s instanceof MemcacheService
            !s.connected

        when: "initialized with a ServiceType, target host, custom port and Environment"
            s = spec.type(ServiceType.MEMCACHE).target(targetHost, targetPort).env(env).get()
        then: "it should be instantiated but not connected"
            s instanceof MemcacheService
            !s.connected
    }
}