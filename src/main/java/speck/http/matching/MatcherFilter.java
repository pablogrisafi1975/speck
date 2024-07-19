/*
 * Copyright 2011- Per Wendel
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

import java.io.IOException;


import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import speck.CustomErrorPages;
import speck.ExceptionMapper;
import speck.HaltException;
import speck.RequestResponseFactory;
import speck.Response;
import speck.embeddedserver.jdkserver.HttpExchangeWrapper;
import speck.route.HttpMethod;
import speck.serialization.SerializerChain;
import speck.staticfiles.StaticFilesConfiguration;

/**
 * Matches Speck routes and filters.
 *
 * @author Per Wendel
 */
public class MatcherFilter extends Filter {

    private static final System.Logger LOG = System.getLogger(MatcherFilter.class.getName());;

    private static final String ACCEPT_TYPE_REQUEST_MIME_HEADER = "Accept";
    private static final String HTTP_METHOD_OVERRIDE_HEADER = "X-HTTP-Method-Override";

    private final StaticFilesConfiguration staticFiles;

    private speck.route.Routes routeMatcher;
    private SerializerChain serializerChain;
    private ExceptionMapper exceptionMapper;

    private boolean hasOtherHandlers;

    /**
     * Constructor
     *
     * @param routeMatcher     The route matcher
     * @param staticFiles      The static files configuration object
     * @param hasOtherHandlers If true, do nothing if request is not consumed by Speck in order to let others handlers process the request.
     */
    public MatcherFilter(speck.route.Routes routeMatcher,
                         StaticFilesConfiguration staticFiles,
                         ExceptionMapper exceptionMapper,
                         boolean hasOtherHandlers) {

        this.routeMatcher = routeMatcher;
        this.staticFiles = staticFiles;
        this.exceptionMapper = exceptionMapper;
        this.hasOtherHandlers = hasOtherHandlers;
        this.serializerChain = new SerializerChain();
    }


    @Override
    public void doFilter(HttpExchange httpExchange, Chain chain) throws IOException {


        // handle static resources
        boolean consumedByStaticFile = staticFiles.consume(httpExchange);

        if (consumedByStaticFile) {
            return;
        }

        String method = getHttpMethodFrom(httpExchange);

        String httpMethodStr = method.toLowerCase();
        //https://stackoverflow.com/questions/4931323/whats-the-difference-between-getrequesturi-and-getpathinfo-methods-in-httpservl
        String uri = httpExchange.getRequestURI().getRawPath();
        String acceptType = httpExchange.getRequestHeaders().getFirst(ACCEPT_TYPE_REQUEST_MIME_HEADER);

        Body body = Body.create();

        RequestWrapper requestWrapper = RequestWrapper.create();
        ResponseWrapper responseWrapper = ResponseWrapper.create();

        Response response = RequestResponseFactory.createResponse(httpExchange);

        HttpMethod httpMethod = HttpMethod.get(httpMethodStr);

        RouteContext context = RouteContext.create()
                .withMatcher(routeMatcher)
                .withHttpRequest(httpExchange)
                .withUri(uri)
                .withAcceptType(acceptType)
                .withBody(body)
                .withRequestWrapper(requestWrapper)
                .withResponseWrapper(responseWrapper)
                .withResponse(response)
                .withHttpMethod(httpMethod);

        try {
            try {

                BeforeFilters.execute(context);
                Routes.execute(context);
                AfterFilters.execute(context);

            } catch (HaltException halt) {

                Halt.modify(httpExchange, body, halt);

            } catch (Exception generalException) {

                GeneralError.modify(
                        httpExchange,
                        body,
                        requestWrapper,
                        responseWrapper,
                        exceptionMapper,
                        generalException);

            }

            // If redirected and content is null set to empty string to not throw NotConsumedException
            if (body.notSet() && responseWrapper.isRedirected()) {
                body.set("");
            }

            if (body.notSet() && hasOtherHandlers) {
                /*
                if (servletRequest instanceof HttpRequestWrapper) {
                    ((HttpRequestWrapper) servletRequest).notConsumed(true);
                    return;
                }*/
            }

            if (body.notSet()) {
                LOG.log(System.Logger.Level.INFO, "The requested route [{0}] has not been mapped in Speck for {1}: [{2}]",
                         uri, ACCEPT_TYPE_REQUEST_MIME_HEADER, acceptType);
                if(httpExchange instanceof HttpExchangeWrapper wrapper){
                    wrapper.setResponseCode(404);
                }

                if (CustomErrorPages.existsFor(404)) {
                     requestWrapper.setDelegate(RequestResponseFactory.createRequest(httpExchange));
                    responseWrapper.setDelegate(RequestResponseFactory.createResponse(httpExchange));
                    body.set(CustomErrorPages.getFor(404, requestWrapper, responseWrapper));
                } else {
                    body.set(String.format(CustomErrorPages.NOT_FOUND));
                }
            }
        } finally {
            try {
                AfterAfterFilters.execute(context);
            } catch (Exception generalException) {
                GeneralError.modify(
                        httpExchange,
                        body,
                        requestWrapper,
                        responseWrapper,
                        exceptionMapper,
                        generalException);
            }
        }

        if (body.isSet()) {
            body.serializeTo(httpExchange, serializerChain);
        } else if (chain != null) {
            chain.doFilter(httpExchange);
        }
    }

    private String getHttpMethodFrom(HttpExchange exchange) {
        String method = exchange.getRequestHeaders().getFirst(HTTP_METHOD_OVERRIDE_HEADER);

        if (method == null) {
            method = exchange.getRequestMethod();
        }
        return method;
    }





    @Override
    public String description() {
        return "Speck filter";
    }
}
