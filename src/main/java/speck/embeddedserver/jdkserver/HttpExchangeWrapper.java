/*
 * Copyright 2015 - Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package speck.embeddedserver.jdkserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import speck.utils.IOUtils;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

/**
 * Http request wrapper. Wraps the request so 'getInputStream()' can be called multiple times.
 * Also has methods for checking if request has been consumed.
 */
public class HttpExchangeWrapper extends HttpExchange {
    private final HttpExchange httpExchange;
    private byte[] cachedInputBytes;
    private boolean notConsumed = false;
    private boolean committed = false;

    private Integer responseCode;

    public HttpExchangeWrapper(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    public boolean notConsumed() {
        return notConsumed;
    }

    public void notConsumed(boolean notConsumed) {
        this.notConsumed = notConsumed;
    }

    @Override
    public Headers getRequestHeaders() {
        return httpExchange.getRequestHeaders();
    }

    @Override
    public Headers getResponseHeaders() {
        return httpExchange.getResponseHeaders();
    }

    @Override
    public URI getRequestURI() {
        return httpExchange.getRequestURI();
    }

    @Override
    public String getRequestMethod() {
        return httpExchange.getRequestMethod();
    }

    @Override
    public HttpContext getHttpContext() {
        return httpExchange.getHttpContext();
    }

    @Override
    public void close() {
        httpExchange.close();
    }




    @Override
    public OutputStream getResponseBody() {
        return httpExchange.getResponseBody();
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        httpExchange.sendResponseHeaders(rCode, responseLength);
        committed = true;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return httpExchange.getRemoteAddress();
    }

    @Override
    public int getResponseCode() {
        return responseCode != null ? responseCode : httpExchange.getResponseCode();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return httpExchange.getLocalAddress();
    }

    @Override
    public String getProtocol() {
        return httpExchange.getProtocol();
    }

    @Override
    public Object getAttribute(String name) {
        return httpExchange.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        httpExchange.setAttribute(name, value);
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {
        httpExchange.setStreams(i, o);
    }

    @Override
    public HttpPrincipal getPrincipal() {
        return httpExchange.getPrincipal();
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public boolean isCommitted() {
        return committed;
    }

    @Override
    public InputStream getRequestBody() {
        // disable stream cache for chunked transfer encoding
        String transferEncoding = httpExchange.getRequestHeaders().getFirst("Transfer-Encoding");
        if ("chunked".equals(transferEncoding)) {
            return httpExchange.getRequestBody();
        }
        // disable stream cache for multipart/form-data file upload
        // -> upload might be very large and might lead to out-of-memory error if we try to cache the bytes
        String contentType = httpExchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            return httpExchange.getRequestBody();
        }
        if (cachedInputBytes == null) {
            cacheInputStream();
        }
        return new CachedInputStream();
    }

    private void cacheInputStream() {
        try {
            cachedInputBytes = IOUtils.toByteArray(httpExchange.getRequestBody());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private class CachedInputStream extends InputStream {
        private ByteArrayInputStream byteArrayInputStream;

        public CachedInputStream() {
            byteArrayInputStream = new ByteArrayInputStream(cachedInputBytes);
        }

        @Override
        public int read() {
            return byteArrayInputStream.read();
        }

        @Override
        public int available() {
            return byteArrayInputStream.available();
        }


    }
}
