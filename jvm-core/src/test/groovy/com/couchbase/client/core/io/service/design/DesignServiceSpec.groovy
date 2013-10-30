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

package com.couchbase.client.core.io.service.design

import spock.lang.Specification

/**
 * Verifies the functionality of the SUT {@link DesignService}.
 */
class DesignServiceSpec extends Specification {

    def "A DesignService should have a default port defined"() {
        expect:
            DesignService.DEFAULT_PORT > 0
    }

    def "A DesignService should transform connection errors into a Message"() {
        when: "The node is not available"
        then:
            thrown(IllegalArgumentException)

        when: "The address of the remote node cannot be resolved"
        then:
            thrown(IllegalArgumentException)

        when: "The connection to the remote node is refused"
        then:
            thrown(IllegalArgumentException)

        when: "The connection to the remote node times out"
        then:
            thrown(IllegalArgumentException)

        when: "The node is available but the port is not"
        then:
            thrown(IllegalArgumentException)
    }

    // todo: handle reconnect
    // todo: handle closes
    // todo: handle successful ops
    // todo: handle failing ops

}
