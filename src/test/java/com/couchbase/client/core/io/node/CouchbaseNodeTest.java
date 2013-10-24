package com.couchbase.client.core.io.node;

import com.couchbase.client.core.io.service.Service;
import com.couchbase.client.core.io.service.ServiceType;
import com.couchbase.client.core.io.service.design.DesignService;
import com.couchbase.client.core.io.service.message.ConnectStatus;
import com.couchbase.client.core.io.service.spec.ServiceSpec;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.composable.Deferred;
import reactor.core.composable.Promise;
import reactor.core.composable.spec.Promises;
import reactor.core.spec.Reactors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Verifies functionality of an isolated {@link CouchbaseNode}.
 */
public class CouchbaseNodeTest {

    private final Environment ENV = new Environment();
    private final Reactor reactor = Reactors.reactor(ENV);

    private final Promise<ConnectStatus> connected =
        Promises.<ConnectStatus>success(ConnectStatus.connected()).get();
    private final Service mockedService = Mockito.mock(DesignService.class);
    private final ServiceSpec mockedSpec = Mockito.mock(ServiceSpec.class);

    @Test
    public void shouldAddService() throws Exception {
        when(mockedService.connect()).thenReturn(connected);
        when(mockedSpec.type(ServiceType.DESIGN)).thenReturn(mockedSpec);
        when(mockedSpec.get()).thenReturn(mockedService);

        CouchbaseNode node = new CouchbaseNode(reactor, mockedSpec);
        Promise<ConnectStatus> connectPromise = node.addService(ServiceType.DESIGN, "default");
        assertThat(connectPromise.await().state(), is(ConnectStatus.State.CONNECTED));
        assertThat(node.hasService(ServiceType.DESIGN, "default"), is(true));
    }

    @Test
    public void shouldRemoveService() throws Exception {
        when(mockedService.connect()).thenReturn(connected);
        Deferred<Void,Promise<Void>> disconnectedDeferred = Promises.<Void>defer().get();
        disconnectedDeferred.accept((Void)null);
        when(mockedService.disconnect()).thenReturn(disconnectedDeferred.compose());
        when(mockedSpec.type(ServiceType.DESIGN)).thenReturn(mockedSpec);
        when(mockedSpec.get()).thenReturn(mockedService);

        CouchbaseNode node = new CouchbaseNode(reactor, mockedSpec);
        node.addService(ServiceType.DESIGN, "default").await();
        assertThat(node.hasService(ServiceType.DESIGN, "default"), is(true));
        node.removeService(ServiceType.DESIGN, "default").await();
        assertThat(node.hasService(ServiceType.DESIGN, "default"), is(false));
    }
}
