package com.couchbase.client.core.io.service.spec

import com.couchbase.client.core.io.service.Service
import com.couchbase.client.core.io.service.ServiceType
import com.couchbase.client.core.io.service.design.DesignService
import com.couchbase.client.core.io.service.memcache.MemcacheService
import reactor.core.Environment
import spock.lang.Specification

class ServiceSpecSpec extends Specification {

    static final String targetHost = "127.0.0.1"

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

        when: "initialized without an environment"
            spec.type(ServiceType.DESIGN).target(targetHost).get()
        then: "throws an exception"
            e = thrown(IllegalArgumentException)
            e.message == "A Environment needs to be specified."

        when: "initialized with all properties"
            Service s = spec.type(ServiceType.DESIGN).target(targetHost).env(env).get()
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
    }

    def "A ServiceSpec is able to construct a MemcacheService"() {
        when: "initialized with a ServiceType, target host and Environment"
            Service s = spec.type(ServiceType.MEMCACHE).target(targetHost).env(env).get()
        then: "it should be instantiated but not connected"
            s instanceof MemcacheService
            !s.connected
    }
}