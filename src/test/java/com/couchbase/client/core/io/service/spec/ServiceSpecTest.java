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

package com.couchbase.client.core.io.service.spec;

import com.couchbase.client.core.io.service.Service;
import com.couchbase.client.core.io.service.ServiceType;
import com.couchbase.client.core.io.service.design.DesignService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import reactor.core.Environment;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Verifies the correct functionality of a {@link ServiceSpec}.
 */
public class ServiceSpecTest {

    private static final Environment ENV = new Environment();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldThrowWhenServiceTypeNotProvided() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("A ServiceType needs to be specified.");
        new ServiceSpec().get();
    }

    @Test
    public void shouldThrowWhenTargetNotProvided() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("A target host needs to be specified.");
        new ServiceSpec().type(ServiceType.DESIGN).get();
    }

    @Test
    public void shouldThrowWhenEnvironmentNotProvided() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("A Environment needs to be specified.");
        new ServiceSpec().type(ServiceType.DESIGN).target("127.0.0.1", 8091).get();
    }

    @Test
    public void shouldCreateADesignService() {
        Service service = new ServiceSpec()
            .target("127.0.0.1", 8092)
            .type(ServiceType.DESIGN)
            .env(ENV)
            .get();
        assertThat(service, instanceOf(DesignService.class));
    }

}
