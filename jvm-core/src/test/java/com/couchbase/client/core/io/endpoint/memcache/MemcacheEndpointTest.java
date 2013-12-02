package com.couchbase.client.core.io.endpoint.memcache;

import com.couchbase.client.core.message.request.memcache.GetRequest;
import com.couchbase.client.core.message.request.memcache.NoopRequest;
import com.couchbase.client.core.message.response.memcache.MemcacheResponse;
import org.junit.Test;
import reactor.core.Environment;
import reactor.core.composable.Promise;
import reactor.event.Event;
import reactor.function.Consumer;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class MemcacheEndpointTest {

    @Test
    public void foo() throws Exception {
        Environment env = new Environment();
        MemcacheEndpoint endpoint1 = new MemcacheEndpoint(new InetSocketAddress(11210), env);
        MemcacheEndpoint endpoint2 = new MemcacheEndpoint(new InetSocketAddress(11210), env);
        MemcacheEndpoint endpoint3 = new MemcacheEndpoint(new InetSocketAddress(11210), env);
        endpoint1.connect().await();
        endpoint2.connect().await();
        endpoint3.connect().await();

        while (true) {
            int runs = 1000;
            //final long start = System.nanoTime();
            final CountDownLatch latch = new CountDownLatch(runs * 3);

            for (int i = 0; i < runs; i++) {
                endpoint1.sendAndReceive(Event.wrap(new GetRequest("foobar" + i))).onComplete(new Consumer<Promise<MemcacheResponse>>() {
                    @Override
                    public void accept(final Promise<MemcacheResponse> promise) {
                        latch.countDown();
                    }
                });
                endpoint2.sendAndReceive(Event.wrap(new GetRequest("foobar" + i))).onComplete(new Consumer<Promise<MemcacheResponse>>() {
                    @Override
                    public void accept(final Promise<MemcacheResponse> promise) {
                        latch.countDown();
                    }
                });
                endpoint3.sendAndReceive(Event.wrap(new GetRequest("foobar" + i))).onComplete(new Consumer<Promise<MemcacheResponse>>() {
                    @Override
                    public void accept(final Promise<MemcacheResponse> promise) {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            //final long end = System.nanoTime();
            //System.out.println(TimeUnit.NANOSECONDS.toMicros(end - start));
        }
    }
}
