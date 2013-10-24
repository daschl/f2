package com.couchbase.client.core.io.service.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.CharsetUtil;

/**
 * Utility methods to ease the HTTP workings.
 */
public class HttpUtils {

    /**
     * Add the HTTP basic auth headers to a netty request.
     *
     * @param request the request to modify.
     * @param user the user of the request.
     * @param password the password of the request.
     */
    public static void addAuth(final HttpRequest request, final String user, final String password) {
        String auth = user + ":" + password;
        ByteBuf encoded = Base64.encode(Unpooled.copiedBuffer(auth, CharsetUtil.UTF_8));
        request.headers().add(HttpHeaders.Names.AUTHORIZATION, encoded.toString(CharsetUtil.UTF_8));
    }

}
