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

package com.couchbase.client.core.io.endpoint.memcache;

import com.couchbase.client.core.message.request.memcache.GetRequest;
import com.couchbase.client.core.message.request.memcache.MemcacheRequest;
import com.couchbase.client.core.message.response.memcache.GetResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.memcache.binary.*;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;


public class MemcacheCodec extends CombinedChannelDuplexHandler<MemcacheCodec.MemcacheDecoder, MemcacheCodec.MemcacheEncoder> {

    private final Queue<Class<?>> queue = new ArrayDeque<Class<?>>();

    public MemcacheCodec() {
        init(new MemcacheDecoder(), new MemcacheEncoder());
    }

    public class MemcacheEncoder extends MessageToMessageEncoder<MemcacheRequest> {

        @Override
        protected void encode(ChannelHandlerContext ctx, MemcacheRequest msg, List<Object> out) throws Exception {
            queue.offer(msg.getClass());

            BinaryMemcacheRequest request = null;
            if (msg instanceof GetRequest) {
                BinaryMemcacheRequestHeader header = new DefaultBinaryMemcacheRequestHeader();
                header.setKeyLength((short) msg.key().length());
                header.setTotalBodyLength((short) msg.key().length());
                request = new DefaultFullBinaryMemcacheRequest(header, msg.key(), Unpooled.EMPTY_BUFFER);
            }

            out.add(request);
        }
    }

    public class MemcacheDecoder extends MessageToMessageDecoder<BinaryMemcacheResponse> {

        @Override
        protected void decode(ChannelHandlerContext ctx, BinaryMemcacheResponse response, List<Object> in) throws Exception {
            Class<?> clazz = queue.poll();


            FullBinaryMemcacheResponse fullResponse = (FullBinaryMemcacheResponse) response;
            if (clazz.equals(GetRequest.class)) {
                decodeGet(in, fullResponse);
            }
        }

        private void decodeGet(List<Object> in, FullBinaryMemcacheResponse response) {
            // real decoding here.
            in.add(new GetResponse());
        }

    }
}
