/*
 * Copyright 2016 - Per Wendel
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
package speck.http.matching;

import com.sun.net.httpserver.HttpExchange;
import speck.embeddedserver.jdkserver.HttpExchangeWrapper;
import speck.serialization.SerializerChain;
import speck.staticfiles.MimeType;
import speck.utils.GzipUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents the 'body'
 */
final class Body {

    private Object content;

    private Body() {

    }

    public static Body create() {
        return new Body();
    }

    public boolean notSet() {
        return content == null;
    }

    public boolean isSet() {
        return content != null;
    }

    public Object get() {
        return content;
    }

    public void set(Object content) {
        this.content = content;
    }

    public void serializeTo(HttpExchange httpExchange,
                            SerializerChain serializerChain) throws IOException {

        if (!(httpExchange instanceof HttpExchangeWrapper wrapper && wrapper.isCommitted())) {
            if (httpExchange.getResponseHeaders().get("Content-Type") == null && MimeType.shouldGuess()) {
                httpExchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            }


            // Serialize the body to output stream
            Integer responseCode =  httpExchange.getResponseCode()  == -1 ? 200 : httpExchange.getResponseCode();

            // Check if GZIP is wanted/accepted and in that case handle that
            //OutputStream responseStream = httpExchange.getResponseBody();//GzipUtils.checkAndWrap(httpRequest, httpResponse, true);
            OutputStream responseStream = GzipUtils.checkAndWrap(httpExchange, true, responseCode);

            //response length 0 means using chinked mode.
        //I don't know the real length until a specific serializer is chosen
        //Another option could be to write into a cache and then read cache length and then write cache to actual ouptput stream
        //Another option could be pass the whole httpExchange to serializers
            serializerChain.process(responseStream, content);

            responseStream.flush(); // needed for GZIP stream. Not sure where the HTTP response actually gets cleaned up
            responseStream.close(); // needed for GZIP
        }
    }


}
