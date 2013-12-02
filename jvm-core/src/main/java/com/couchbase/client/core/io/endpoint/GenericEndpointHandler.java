package com.couchbase.client.core.io.endpoint;

import io.netty.channel.ChannelHandlerAppender;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import reactor.core.composable.Deferred;
import reactor.core.composable.Promise;
import reactor.event.Event;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * This Handler keeps track of all messages coming in to an endpoint, and completes the futures as they
 * succeed or fail.
 *
 * It also makes sure that proper error handling is triggered and the communication with the upper endpoint works
 * as expected by both parties. The actual codecs do only need to manage the actual messages, not the future completion
 * and so on.
 */
public class GenericEndpointHandler<REQ, RES> extends ChannelHandlerAppender {

    private final Queue<Event<REQ>> queue = new ArrayDeque<Event<REQ>>();

    public GenericEndpointHandler() {
        add(new EventResponseDecoder(), new EventRequestEncoder());
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);

        ctx.channel().eventLoop().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ctx.flush();
            }
        }, 0, 50, TimeUnit.MICROSECONDS);
    }

    final class EventResponseDecoder extends MessageToMessageDecoder<RES> {

        @Override
        protected void decode(ChannelHandlerContext ctx, RES in, List<Object> out) throws Exception {
            Event<REQ> event = queue.poll();
            Deferred<RES, Promise<RES>> deferred = (Deferred<RES, Promise<RES>>) event.getReplyTo();
            deferred.accept(in);
        }
    }

    final class EventRequestEncoder extends MessageToMessageEncoder<Event<REQ>> {

        @Override
        protected void encode(ChannelHandlerContext ctx, Event<REQ> msg, List<Object> out) throws Exception {
            queue.offer(msg);
            out.add(msg.getData());
        }

    }

}
