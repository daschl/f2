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

package com.couchbase.client.core.io.node;

import com.couchbase.client.core.io.service.Service;
import com.couchbase.client.core.io.service.ServiceType;
import com.couchbase.client.core.io.service.design.DesignService;
import com.couchbase.client.core.io.service.message.ConnectStatus;
import com.couchbase.client.core.io.service.spec.ServiceSpec;
import com.couchbase.client.core.message.CouchbaseResponse;
import com.couchbase.client.core.message.request.design.HasDesignDocumentRequest;
import com.couchbase.client.core.message.response.design.DesignResponse;
import com.couchbase.client.core.message.response.design.HasDesignDocumentResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import reactor.core.composable.Deferred;
import reactor.core.composable.Promise;
import reactor.core.composable.spec.Promises;
import reactor.event.Event;
import reactor.event.registry.CachingRegistry;
import reactor.event.registry.Registry;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Verifies functionality of an isolated {@link CouchbaseNode}.
 */
public class CouchbaseNodeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Registry<Service> registry;

    private final Promise<ConnectStatus> connected =
        Promises.<ConnectStatus>success(ConnectStatus.connected()).get();
    private final Service mockedDesignService = Mockito.mock(DesignService.class);
    private final ServiceSpec mockedSpec = Mockito.mock(ServiceSpec.class);
    private final Event<HasDesignDocumentRequest> designRequest =
        Event.wrap(new HasDesignDocumentRequest("default", "fooDesign"));
    private final CouchbaseResponse designResponse = new HasDesignDocumentResponse(DesignResponse.Status.FOUND);

    @Before
    public void setupMocks() {
        Deferred<Void,Promise<Void>> disconnectedDeferred = Promises.<Void>defer().get();
        disconnectedDeferred.accept((Void)null);
        when(mockedDesignService.connect()).thenReturn(connected);
        when(mockedDesignService.disconnect()).thenReturn(disconnectedDeferred.compose());

        when(mockedDesignService.sendAndReceive(designRequest))
            .thenReturn(Promises.success(designResponse).get());

        when(mockedSpec.type(ServiceType.DESIGN)).thenReturn(mockedSpec);
        when(mockedSpec.get()).thenReturn(mockedDesignService);
    }

    @Before
    public void resetState() {
        registry = new CachingRegistry<Service>();
    }

    @Test
    public void shouldAddService() throws Exception {
        CouchbaseNode node = new CouchbaseNode(registry, mockedSpec);
        Promise<ConnectStatus> connectPromise = node.addService(ServiceType.DESIGN, "default");
        assertThat(connectPromise.await().state(), is(ConnectStatus.State.CONNECTED));
        assertThat(node.hasService(ServiceType.DESIGN, "default"), is(true));
    }

    @Test
    public void shouldRemoveService() throws Exception {
        CouchbaseNode node = new CouchbaseNode(registry, mockedSpec);
        node.addService(ServiceType.DESIGN, "default").await();
        assertThat(node.hasService(ServiceType.DESIGN, "default"), is(true));
        node.removeService(ServiceType.DESIGN, "default").await();
        assertThat(node.hasService(ServiceType.DESIGN, "default"), is(false));
    }

    @Test
    public void shouldAcceptRequestsToActiveService() throws Exception  {
        CouchbaseNode node = new CouchbaseNode(registry, mockedSpec);
        node.addService(ServiceType.DESIGN, "default").await();
        assertThat(node.hasService(ServiceType.DESIGN, "default"), is(true));

        Promise<? extends CouchbaseResponse> responsePromise =
            node.sendAndReceive(ServiceType.DESIGN, "default", designRequest);
        CouchbaseResponse response = responsePromise.await();
        assertThat(response, instanceOf(HasDesignDocumentResponse.class));
        assertThat(response, sameInstance(designResponse));
    }

    @Test
    public void shouldRejectRequestToNonExistentService() throws Exception {
        CouchbaseNode node = new CouchbaseNode(registry, mockedSpec);
        assertThat(node.hasService(ServiceType.DESIGN, "default"), not(true));

        thrown.expect(ServiceNotFoundException.class);
        thrown.expectMessage("No service of type DESIGN for bucket default registered.");
        node.sendAndReceive(ServiceType.DESIGN, "default", designRequest);
    }

    @Test
    public void shouldShutdownCorrectly() throws Exception {

    }

}
