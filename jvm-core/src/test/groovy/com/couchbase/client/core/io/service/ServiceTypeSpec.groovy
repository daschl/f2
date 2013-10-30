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

package com.couchbase.client.core.io.service

import spock.lang.Specification

/**
 * Verifies the functionality of the SUT {@link ServiceType}.
 *
 * Note that while testing an ENUM does not make much sense in the first place, this Spec is in place to make sure the
 * bound strategies are not changed accidentally. Also, the amount of ENUM elements is counted to make sure this test
 * fails and needs to be adapted if more services get added and/or removed.
 */
class ServiceTypeSpec extends Specification {

    def "A Service type has exactly 2 different types defined"() {
        expect:
            ServiceType.values().size() == 2
    }

    def "A ServiceType needs to bind the following strategies"() {
        expect:
            ServiceType.DESIGN.strategy() == BucketServiceStrategy.ONE_FOR_ALL
            ServiceType.valueOf("DESIGN").strategy() == BucketServiceStrategy.ONE_FOR_ALL

            ServiceType.MEMCACHE.strategy() == BucketServiceStrategy.ONE_BY_ONE
            ServiceType.valueOf("MEMCACHE").strategy() == BucketServiceStrategy.ONE_BY_ONE
    }

}
