package com.couchbase.client.core.io.endpoint.design;

import com.couchbase.client.core.message.request.design.GetDesignDocumentRequest;
import com.couchbase.client.core.message.response.design.DesignResponse;
import org.junit.Test;
import reactor.core.Environment;
import reactor.core.composable.Promise;
import reactor.event.Event;
import reactor.function.Consumer;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DesignEndpointTest {

    @Test
    public void foo() throws Exception {
        Environment env = new Environment();
        DesignEndpoint endpoint = new DesignEndpoint(new InetSocketAddress(8092), env);
        endpoint.connect().await();

        while (true) {
            int runs = 1000;
            final long start = System.nanoTime();
            final CountDownLatch latch = new CountDownLatch(runs);
            for (int i = 0; i < runs; i++) {
                endpoint.sendAndReceive(Event.wrap(new GetDesignDocumentRequest("default", "foo"))).onComplete(new Consumer<Promise<DesignResponse>>() {
                    @Override
                    public void accept(final Promise<DesignResponse> promise) {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            final long end = System.nanoTime();
            System.out.println(TimeUnit.NANOSECONDS.toMillis(end - start));
        }
    }
}
