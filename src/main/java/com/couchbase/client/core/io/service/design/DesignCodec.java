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

package com.couchbase.client.core.io.service.design;

import com.couchbase.client.core.io.service.util.HttpUtils;
import com.couchbase.client.core.message.request.design.DesignRequest;
import com.couchbase.client.core.message.request.design.GetDesignDocumentRequest;
import com.couchbase.client.core.message.request.design.HasDesignDocumentRequest;
import com.couchbase.client.core.message.response.design.DesignResponse;
import com.couchbase.client.core.message.response.design.GetDesignDocumentResponse;
import com.couchbase.client.core.message.response.design.HasDesignDocumentResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

class DesignCodec extends CombinedChannelDuplexHandler<DesignCodec.DesignDecoder, DesignCodec.DesignEncoder> {


    private final Queue<Class<?>> queue = new ArrayDeque<Class<?>>();

    public DesignCodec() {
        init(new DesignDecoder(), new DesignEncoder());
    }

    public class DesignEncoder extends MessageToMessageEncoder<DesignRequest> {

        private final HttpVersion version = HttpVersion.HTTP_1_1;

        @Override
        protected void encode(ChannelHandlerContext ctx, DesignRequest msg, List<Object> out) throws Exception {
            queue.offer(msg.getClass());

            HttpRequest request = null;
            if (msg instanceof HasDesignDocumentRequest) {
                request = new DefaultFullHttpRequest(version, HttpMethod.HEAD, msg.path());
            } else if(msg instanceof GetDesignDocumentRequest) {
                request = new DefaultFullHttpRequest(version, HttpMethod.GET, msg.path());
            }

            HttpUtils.addAuth(request, msg.user(), msg.password());
            out.add(request);
        }
    }

    public class DesignDecoder extends MessageToMessageDecoder<HttpResponse> {

        @Override
        protected void decode(ChannelHandlerContext ctx, HttpResponse response, List<Object> in) throws Exception {
            Class<?> clazz = queue.poll();
            DesignResponse.Status status = decodeResponseCode(response.getStatus().code());

            FullHttpResponse fullResponse = (FullHttpResponse) response;

            if (clazz.equals(HasDesignDocumentRequest.class)) {
                decodeHasDesignDocument(in, status);
            } else if (clazz.equals(GetDesignDocumentRequest.class)) {
                decodeGetDesignDocument(fullResponse, in, status);
            }
        }

        private void decodeHasDesignDocument(List<Object> in, DesignResponse.Status status) {
            in.add(new HasDesignDocumentResponse(status));
        }

        private void decodeGetDesignDocument(FullHttpResponse response, List<Object> in, DesignResponse.Status status) {
            String body = response.content().toString(CharsetUtil.UTF_8);
            in.add(new GetDesignDocumentResponse(status, body));
        }

        /**
         * Maps the potential HTTP responses to internal response enums.
         *
         * @param code the code to convert.
         * @return the converted Status.
         */
        private DesignResponse.Status decodeResponseCode(int code) {
            DesignResponse.Status status;

            switch(code) {
                case 200:
                case 201:
                case 202:
                    status = DesignResponse.Status.FOUND;
                    break;
                case 300:
                case 301:
                case 302:
                case 303:
                    status = DesignResponse.Status.REDIRECT;
                    break;
                case 401:
                    status = DesignResponse.Status.UNAUTHORIZED;
                    break;
                case 404:
                    status = DesignResponse.Status.NOT_FOUND;
                    break;
                case 500:
                case 501:
                case 502:
                case 503:
                    status = DesignResponse.Status.SERVER_ERROR;
                    break;
                default:
                    throw new IllegalStateException("Found unhandled server code: " + code);
            }

            return status;
        }

    }
}
