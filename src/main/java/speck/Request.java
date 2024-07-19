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
package speck;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import speck.routematch.RouteMatch;
import speck.utils.CollectionUtils;
import speck.utils.IOUtils;
import speck.utils.SpeckUtils;
import speck.utils.StringUtils;
import speck.utils.urldecoding.UrlDecode;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides information about the HTTP request
 *
 * @author Per Wendel
 */
public class Request {

    private static final System.Logger LOG = System.getLogger(Request.class.getName());
    ;

    private static final String USER_AGENT = "user-agent";

    private Map<String, String> params;
    private List<String> splat;
    private QueryParamsMap queryMap;

    private HttpExchange httpExchange;

    private boolean validSession = false;
    private String matchedPath = null;


    /* Lazy loaded stuff */
    private String body = null;
    private byte[] bodyAsBytes = null;

    private Set<String> headers = null;

    //    request.body              # request body sent by the client (see below), DONE
    //    request.scheme            # "http"                                DONE
    //    request.path_info         # "/foo",                               DONE
    //    request.port              # 80                                    DONE
    //    request.request_method    # "GET",                                DONE
    //    request.query_string      # "",                                   DONE
    //    request.content_length    # length of request.body,               DONE
    //    request.media_type        # media type of request.body            DONE, content type?
    //    request.host              # "example.com"                         DONE
    //    request["SOME_HEADER"]    # value of SOME_HEADER header,          DONE
    //    request.user_agent        # user agent (used by :agent condition) DONE
    //    request.url               # "http://example.com/example/foo"      DONE
    //    request.ip                # client IP address                     DONE
    //    request.env               # raw env hash handed in by Rack,       DONE
    //    request.get?              # true (similar methods for other verbs)
    //    request.secure?           # false (would be true over ssl)
    //    request.forwarded?        # true (if running behind a reverse proxy)
    //    request.cookies           # hash of browser cookies,              DONE
    //    request.xhr?              # is this an ajax request?
    //    request.script_name       # "/example"
    //    request.form_data?        # false
    //    request.referrer          # the referrer of the client or '/'

    protected Request() {
        // Used by wrapper
    }

    /**
     * Constructor
     *
     * @param match   the route match
     * @param request the servlet request
     */
    Request(RouteMatch match, HttpExchange request) {
        this.httpExchange = request;
        this.matchedPath = match.getMatchUri();
        changeMatch(match);
    }

    /**
     * Constructor - Used to create a request and no RouteMatch is available.
     *
     * @param request the servlet request
     */
    Request(HttpExchange request) {
        this.httpExchange = request;

        // Empty
        params = new HashMap<>();
        splat = new ArrayList<>();
    }

    private static Map<String, String> getParams(List<String> request, List<String> matched) {
        Map<String, String> params = new HashMap<>();

        for (int i = 0; (i < request.size()) && (i < matched.size()); i++) {
            String matchedPart = matched.get(i);

            if (SpeckUtils.isParam(matchedPart)) {

                String decodedReq = UrlDecode.path(request.get(i));

                LOG.log(System.Logger.Level.DEBUG, "matchedPart: "
                    + matchedPart
                    + " = "
                    + decodedReq);

                params.put(matchedPart.toLowerCase(), decodedReq);
            }
        }
        return Collections.unmodifiableMap(params);
    }

    private static List<String> getSplat(List<String> request, List<String> matched) {
        int nbrOfRequestParts = request.size();
        int nbrOfMatchedParts = matched.size();

        boolean sameLength = (nbrOfRequestParts == nbrOfMatchedParts);

        List<String> splat = new ArrayList<>();

        for (int i = 0; (i < nbrOfRequestParts) && (i < nbrOfMatchedParts); i++) {

            String matchedPart = matched.get(i);

            if (SpeckUtils.isSplat(matchedPart)) {

                StringBuilder splatParam = new StringBuilder(request.get(i));

                if (!sameLength && (i == (nbrOfMatchedParts - 1))) {
                    for (int j = i + 1; j < nbrOfRequestParts; j++) {
                        splatParam.append("/");
                        splatParam.append(request.get(j));
                    }
                }
                try {
                    String decodedSplat = URLDecoder.decode(splatParam.toString(), "UTF-8");
                    splat.add(decodedSplat);
                } catch (UnsupportedEncodingException e) {
                }
            }
        }

        return Collections.unmodifiableList(splat);
    }

    protected void changeMatch(RouteMatch match) {
        List<String> requestList = SpeckUtils.convertRouteToList(match.getRequestURI());
        List<String> matchedList = SpeckUtils.convertRouteToList(match.getMatchUri());

        this.matchedPath = match.getMatchUri();
        params = getParams(requestList, matchedList);
        splat = getSplat(requestList, matchedList);
    }

    /**
     * Returns the map containing all route params
     *
     * @return a map containing all route params
     */
    public Map<String, String> params() {
        return Collections.unmodifiableMap(params);
    }

    /**
     * Returns the value of the provided route pattern parameter.
     * Example: parameter 'name' from the following pattern: (get '/hello/:name')
     *
     * @param param the param
     * @return null if the given param is null or not found
     */
    public String params(String param) {
        if (param == null) {
            return null;
        }

        if (param.startsWith(":")) {
            return params.get(param.toLowerCase()); // NOSONAR
        } else {
            return params.get(":" + param.toLowerCase()); // NOSONAR
        }
    }

    /**
     * @return an array containing the splat (wildcard) parameters
     */
    public String[] splat() {
        return splat.toArray(new String[0]);
    }

    /**
     * @return request method e.g. GET, POST, PUT, ...
     */
    public String requestMethod() {
        return httpExchange.getRequestMethod();
    }

    /**
     * @return the scheme
     */
    public String scheme() {
        return httpExchange.getProtocol();
    }

    /**
     * @return the host
     */
    public String host() {
        return getFirstHeaderValueOrNull("host");
    }

    /**
     * @return the user-agent
     */
    public String userAgent() {
        return getFirstHeaderValueOrNull(USER_AGENT);
    }

    /**
     * @return the server port
     */
    public int port() {
        return httpExchange.getRequestURI().getPort();
    }

    /**
     * @return the path info
     * Example return: "/example/foo"
     */
    public String pathInfo() {
        return httpExchange.getRequestURI().getPath();
    }

    /**
     * @return the matched route
     * Example return: "/account/:accountId"
     */
    public String matchedPath() {
        return this.matchedPath;
    }

    /**
     * @return the servlet path
     */
    public String servletPath() {
        return httpExchange.getRequestURI().getPath();
    }

    /**
     * @return the context path
     */
    public String contextPath() {
        return httpExchange.getRequestURI().getPath();
    }

    /**
     * @return the URL string
     */
    public String url() {
        return httpExchange.getRequestURI().toString();
    }

    /**
     * @return the content type of the body
     */
    public String contentType() {
        return getFirstHeaderValueOrNull("Content-Type");
    }

    /**
     * @return the client's IP address
     */
    public String ip() {
        String forwardedFor = httpExchange.getRequestHeaders().getFirst("X-forwarded-for");
        if (forwardedFor != null) {
            return forwardedFor;
        }
        return httpExchange.getRemoteAddress().toString();
    }

    /**
     * @return the request body sent by the client
     */
    public String body() {

        if (body == null) {
            body = StringUtils.toString(bodyAsBytes(), "UTF-8");//geservletRequest.getCharacterEncoding());
        }

        return body;
    }

    public byte[] bodyAsBytes() {
        if (bodyAsBytes == null) {
            readBodyAsBytes();
        }
        return bodyAsBytes;
    }

    private void readBodyAsBytes() {
        try {
            bodyAsBytes = IOUtils.toByteArray(httpExchange.getRequestBody());
        } catch (Exception e) {
            LOG.log(System.Logger.Level.WARNING, "Exception when reading body", e);
        }
    }

    //CS304 Issue link:https://github.com/perwendel/speck/issues/1061

    /**
     * @return the length of request.body
     */
    public int contentLength() {
        //TODO
        return 0;
        //return servletRequest.getReContentLength();
    }

    /**
     * Gets the query param
     *
     * @param queryParam the query parameter
     * @return the value of the provided queryParam
     * Example: query parameter 'id' from the following request URI: /hello?id=foo
     */
    public String queryParams(String queryParam) {
        List<String> strings = UrlDecode.splitQuery(httpExchange.getRequestURI().getQuery()).get(queryParam);
        return CollectionUtils.isEmpty(strings) ? null : strings.get(0);
    }

    /**
     * Gets the query param and encode it
     *
     * @param queryParam the query parameter
     * @return the encode value of the provided queryParam
     * Example: query parameter 'me' from the URI: /hello?id=fool.
     */
    public String queryParamsSafe(final String queryParam) {
        return Base64.encode(httpExchange.getRequestURI().getQuery());
    }

    /**
     * Gets the query param, or returns default value
     *
     * @param queryParam   the query parameter
     * @param defaultValue the default value
     * @return the value of the provided queryParam, or default if value is null
     * Example: query parameter 'id' from the following request URI: /hello?id=foo
     */
    public String queryParamOrDefault(String queryParam, String defaultValue) {
        String value = queryParams(queryParam);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets all the values of the query param
     * Example: query parameter 'id' from the following request URI: /hello?id=foo&amp;id=bar
     *
     * @param queryParam the query parameter
     * @return the values of the provided queryParam, null if it doesn't exists
     */
    public String[] queryParamsValues(String queryParam) {
        return UrlDecode.splitQuery(httpExchange.getRequestURI().getQuery()).get(queryParam).toArray(new String[]{});
    }

    /**
     * Gets the value for the provided header
     *
     * @param header the header
     * @return the value of the provided header
     */
    public String headers(String header) {
        return getFirstHeaderValueOrNull(header);
    }

    /**
     * @return all query parameters
     */
    public Set<String> queryParams() {
        return UrlDecode.splitQuery(httpExchange.getRequestURI().getQuery()).keySet();
    }

    /**
     * @return all headers
     */
    public Set<String> headers() {
        if (headers == null) {
            headers = Set.copyOf(this.httpExchange.getRequestHeaders().keySet());
        }
        return headers;
    }

    /**
     * @return the query string
     */
    public String queryString() {
        return httpExchange.getRequestURI().getQuery();
    }

    /**
     * Sets an attribute on the request (can be fetched in filters/routes later in the chain)
     *
     * @param attribute The attribute
     * @param value     The attribute value
     */
    public void attribute(String attribute, Object value) {
        httpExchange.setAttribute(attribute, value);
    }

    /**
     * Gets the value of the provided attribute
     *
     * @param attribute The attribute value or null if not present
     * @param <T>       the type parameter.
     * @return the value for the provided attribute
     */
    @SuppressWarnings("unchecked")
    public <T> T attribute(String attribute) {
        return (T) httpExchange.getAttribute(attribute);
    }

    /**
     * @return all attributes
     */
    public Set<String> attributes() {
        return Set.copyOf(httpExchange.getHttpContext().getAttributes().keySet());
    }

    /**
     * @return the raw HttpExchange object handed in by Jetty
     */
    public HttpExchange raw() {
        return httpExchange;
    }

    /**
     * @return the query map
     */
    public QueryParamsMap queryMap() {
        initQueryMap();

        return queryMap;
    }

    /**
     * @param key the key
     * @return the query map
     */
    public QueryParamsMap queryMap(String key) {
        return queryMap().get(key);
    }

    private void initQueryMap() {
        if (queryMap == null) {
            queryMap = new QueryParamsMap(raw());
        }
    }

    /**
     * @return request cookies (or empty Map if cookies aren't present)
     */
    public Map<String, String> cookies() {
        String cookieHeaderValue = getFirstHeaderValueOrNull("Cookie");
        if (cookieHeaderValue == null || cookieHeaderValue.length() == 0) {
            return Map.of();
        }
        List<HttpCookie> httpCookies = HttpCookie.parse(cookieHeaderValue);

        var result = new HashMap<String, String>();

        if (httpCookies != null) {
            for (var cookie : httpCookies) {
                result.put(cookie.getName(), cookie.getValue());
            }
        }
        return result;
    }

    /**
     * Gets cookie by name.
     *
     * @param name name of the cookie
     * @return cookie value or null if the cookie was not found
     */
    public String cookie(String name) {
        String cookieHeaderValue = getFirstHeaderValueOrNull("Cookie");
        if (cookieHeaderValue == null || cookieHeaderValue.length() == 0) {
            return null;
        }
        List<HttpCookie> httpCookies = HttpCookie.parse(cookieHeaderValue);
        if (httpCookies != null) {
            for (var cookie : httpCookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * @return the part of this request's URL from the protocol name up to the query string in the first line of the HTTP request.
     */
    public String uri() {
        return httpExchange.getRequestURI().toString();
    }

    /**
     * @return Returns the name and version of the protocol the request uses
     */
    public String protocol() {
        return httpExchange.getProtocol();
    }

    /**
     * Set the session validity
     *
     * @param validSession the session validity
     */
    void validSession(boolean validSession) {
        this.validSession = validSession;
    }

    private String getFirstHeaderValueOrNull(String header) {
        Headers requestHeaders = this.httpExchange.getRequestHeaders();
        if (requestHeaders == null) {
            return null;
        }
        return requestHeaders.getFirst(header);
    }

}
