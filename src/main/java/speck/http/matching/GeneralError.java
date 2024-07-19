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
import speck.CustomErrorPages;
import speck.ExceptionHandlerImpl;
import speck.ExceptionMapper;
import speck.RequestResponseFactory;
import speck.embeddedserver.jdkserver.HttpExchangeWrapper;

/**
 * Modifies the HTTP response and body based on the provided exception and request/response wrappers.
 */
final class GeneralError {

    private static final System.Logger LOG = System.getLogger(GeneralError.class.getName());


    /**
     * Modifies the HTTP response and body based on the provided exception.
     */
    static void modify(HttpExchange httpExchange,
                       Body body,
                       RequestWrapper requestWrapper,
                       ResponseWrapper responseWrapper,
                       ExceptionMapper exceptionMapper,
                       Exception e) {

        ExceptionHandlerImpl<Exception> handler = (ExceptionHandlerImpl<Exception>) exceptionMapper.getHandler(e);

        if (handler != null) {
            handler.handle(e, requestWrapper, responseWrapper);
            String bodyAfterFilter = responseWrapper.getDelegate().body();

            if (bodyAfterFilter != null) {
                body.set(bodyAfterFilter);
            }
        } else {
            LOG.log(System.Logger.Level.ERROR, "", e);

            if(httpExchange instanceof HttpExchangeWrapper wrapper){
                wrapper.setResponseCode(500);
            }

            if (CustomErrorPages.existsFor(500)) {
                requestWrapper.setDelegate(RequestResponseFactory.createRequest(httpExchange));
                responseWrapper.setDelegate(RequestResponseFactory.createResponse(httpExchange));
                body.set(CustomErrorPages.getFor(500, requestWrapper, responseWrapper));
            } else {
                body.set(CustomErrorPages.INTERNAL_ERROR);
            }
        }
    }

}
