package com.couchbase.client.core.io.service.design;

import com.couchbase.client.core.io.service.Service;
import com.couchbase.client.core.io.service.message.ConnectStatus;
import com.couchbase.client.core.message.request.design.DesignRequest;
import com.couchbase.client.core.message.request.design.GetDesignDocumentRequest;
import com.couchbase.client.core.message.request.design.HasDesignDocumentRequest;
import com.couchbase.client.core.message.response.design.DesignResponse;
import com.couchbase.client.core.message.response.design.GetDesignDocumentResponse;
import com.couchbase.client.core.message.response.design.HasDesignDocumentResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import reactor.core.Environment;
import reactor.core.composable.Deferred;
import reactor.core.composable.Promise;
import reactor.core.composable.spec.Promises;
import reactor.event.Event;
import reactor.function.Consumer;
import reactor.tcp.TcpClient;
import reactor.tcp.TcpConnection;

import java.net.InetSocketAddress;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Verifies the correct functionality of a {@link com.couchbase.client.core.io.service.design.DesignService}.
 *
 * Since the actual messages are more or less passed through, they should be tested in their respective codec. In
 * this test, connecting/disconnect and such is tested and a subset of the messages is used to verified principal
 * functionality.
 */
public class DesignServiceTest {

    private static final Environment ENV = new Environment();
    private static final InetSocketAddress INVALID_HOST = new InetSocketAddress("invalidHost", 8092);
    private static final InetSocketAddress INVALID_PORT = new InetSocketAddress("127.0.0.1", 9934);

    private final TcpClient mockedClient = Mockito.mock(TcpClient.class);
    private final TcpConnection mockedConnection = Mockito.mock(TcpConnection.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldFailConnectOnInvalidHost() throws Exception {
        thrown.expect(UnresolvedAddressException.class);
        new DesignService(INVALID_HOST, ENV).connect().await();
    }

    @Test
    public void shouldFailConnectOnInvalidPort() throws Exception {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Connection refused: /127.0.0.1:9934");
        new DesignService(INVALID_PORT, ENV).connect().await();
    }

    @Test
    public void shouldContainFailureInMessage() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        new DesignService(INVALID_PORT, ENV).connect().onComplete(new Consumer<Promise<ConnectStatus>>() {
            @Override
            public void accept(Promise<ConnectStatus> promise) {
                assertThat(promise.isSuccess(), is(false));
                assertThat(promise.isError(), is(true));
                assertThat(promise.reason(), notNullValue());

                latch.countDown();
            }
        });
        assertThat(latch.await(5, TimeUnit.SECONDS), is(true));
    }

    @Test
    public void shouldConnectProperly() throws Exception {
        when(mockedClient.open()).thenReturn(Promises.success(mockedConnection).get());

        Service service = new DesignService(mockedClient, ENV);
        final CountDownLatch latch = new CountDownLatch(1);
        service.connect().onComplete(new Consumer<Promise<ConnectStatus>>() {
            @Override
            public void accept(Promise<ConnectStatus> result) {
                assertThat(result.isSuccess(), is(true));
                assertThat(result.get(), instanceOf(ConnectStatus.class));
                assertThat(result.get().state(), is(ConnectStatus.State.CONNECTED));
                latch.countDown();
            }
        });
        assertThat(latch.await(5, TimeUnit.SECONDS), is(true));
    }

    @Test
    public void shouldReturnSuccessWhenAlreadyConnected() throws Exception {
        when(mockedClient.open()).thenReturn(Promises.success(mockedConnection).get());

        Service service = new DesignService(mockedClient, ENV);
        assertThat(((ConnectStatus) service.connect().await()).state(), is(ConnectStatus.State.CONNECTED));
        assertThat(((ConnectStatus) service.connect().await()).state(), is(ConnectStatus.State.ALREADY_CONNECTED));
    }

    @Test
    public void shouldPreventDuplicateConnectAttempt() throws Exception {
        Deferred<TcpConnection,Promise<TcpConnection>> waitingDeferred = Promises.defer(ENV, Environment.THREAD_POOL);
        when(mockedClient.open()).thenReturn(waitingDeferred.compose());

        Service service = new DesignService(mockedClient, ENV);
        Promise<ConnectStatus> firstAttempt = service.connect();
        Promise<ConnectStatus> secondAttempt = service.connect();

        assertThat(secondAttempt.await().state(), is(ConnectStatus.State.STILL_CONNECTING));
        waitingDeferred.accept(mockedConnection);
        assertThat(firstAttempt.await().state(), is(ConnectStatus.State.CONNECTED));
    }

    @Test
    public void shouldHandleDisconnect() throws Exception {
        // TODO
    }

    @Test
    public void shouldSwallowDuplicateDisconnect() throws Exception {
        // TODO
    }

    @Test
    public void shouldHandleDisconnectWhenNotConnected() throws Exception {
        // TODO
    }

    @Test
    public void shouldFindExistingDesignDocument() throws Exception {
        DesignResponse.Status status = DesignResponse.Status.FOUND;
        DesignRequest request = new HasDesignDocumentRequest("default", "existingDesign");
        Promise<HasDesignDocumentResponse> mockResponse = Promises.success(new HasDesignDocumentResponse(status)).get();

        when(mockedClient.open()).thenReturn(Promises.success(mockedConnection).get());
        when(mockedConnection.sendAndReceive(request)).thenReturn(mockResponse);

        Service service = new DesignService(mockedClient, ENV);
        assertThat(((ConnectStatus) service.connect().await()).state(), is(ConnectStatus.State.CONNECTED));
        DesignResponse response = (DesignResponse) ((Promise)service.apply(Event.wrap(request))).await();
        assertThat(response, instanceOf(HasDesignDocumentResponse.class));
        assertThat(response.status(), is(status));
    }

    @Test
    public void shouldNotFindNonExistingDesignDocument() throws Exception {
        DesignResponse.Status status = DesignResponse.Status.NOT_FOUND;
        DesignRequest request = new HasDesignDocumentRequest("default", "nonExistent");
        Promise<HasDesignDocumentResponse> mockResponse = Promises.success(new HasDesignDocumentResponse(status)).get();
        when(mockedClient.open()).thenReturn(Promises.success(mockedConnection).get());
        when(mockedConnection.sendAndReceive(request)).thenReturn(mockResponse);

        Service service = new DesignService(mockedClient, ENV);
        assertThat(((ConnectStatus) service.connect().await()).state(), is(ConnectStatus.State.CONNECTED));
        DesignResponse response = (DesignResponse) ((Promise)service.apply(Event.wrap(request))).await();
        assertThat(response, instanceOf(HasDesignDocumentResponse.class));
        assertThat(response.status(), is(status));
    }

    @Test
    public void shouldFindDesignDocumentWithContent() throws Exception {
        DesignResponse.Status status = DesignResponse.Status.FOUND;
        DesignRequest request = new GetDesignDocumentRequest("default", "foundDesignDoc");
        Promise<GetDesignDocumentResponse> mockResponse =
            Promises.success(new GetDesignDocumentResponse(status, "content")).get();
        when(mockedClient.open()).thenReturn(Promises.success(mockedConnection).get());
        when(mockedConnection.sendAndReceive(request)).thenReturn(mockResponse);

        Service service = new DesignService(mockedClient, ENV);
        assertThat(((ConnectStatus) service.connect().await()).state(), is(ConnectStatus.State.CONNECTED));
        GetDesignDocumentResponse response = (GetDesignDocumentResponse) ((Promise)service.apply(Event.wrap(request))).await();
        assertThat(response, instanceOf(GetDesignDocumentResponse.class));
        assertThat(response.status(), is(status));
        assertThat(response.content(), is("content"));
    }

}
