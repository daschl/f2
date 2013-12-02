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
        }, 0, 75, TimeUnit.MICROSECONDS);
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
