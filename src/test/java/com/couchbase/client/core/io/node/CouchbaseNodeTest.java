package com.couchbase.client.core.io.node;

import com.couchbase.client.core.io.service.Service;
import com.couchbase.client.core.io.service.ServiceType;
import com.couchbase.client.core.io.service.design.DesignService;
import com.couchbase.client.core.io.service.message.ConnectStatus;
import com.couchbase.client.core.message.CouchbaseResponse;
import com.couchbase.client.core.message.request.design.DesignRequest;
import com.couchbase.client.core.message.request.design.GetDesignDocumentRequest;
import org.junit.Test;
import reactor.core.Environment;
import reactor.core.composable.Promise;
import reactor.event.Event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 10/22/13
 * Time: 5:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class CouchbaseNodeTest {

    @Test
    public void shouldFoo() throws Exception {

        CouchbaseNode node = new CouchbaseNode("127.0.0.1", new Environment());

        Promise<ConnectStatus> connectPromise = node.addService(ServiceType.DESIGN, "default");
        ConnectStatus status = connectPromise.await();
        assertThat(status.state(), is(ConnectStatus.State.CONNECTED));
        assertThat(node.hasService(ServiceType.DESIGN, "default"), is(true));

        Promise<Void> disconnectPromise = node.removeService(ServiceType.DESIGN, "default");
        disconnectPromise.await();
        assertThat(node.hasService(ServiceType.DESIGN, "defaukt"), is(false));

    }
}
