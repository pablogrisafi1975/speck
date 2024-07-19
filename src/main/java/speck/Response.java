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


import com.sun.net.httpserver.HttpExchange;
import speck.embeddedserver.jdkserver.HttpExchangeWrapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Provides functionality for modifying the response
 *
 * @author Per Wendel
 */
public class Response {

    /**
     * The logger.
     */
    private static final System.Logger LOG = System.getLogger(Response.class.getName());

    private HttpExchange httpExchange;
    private String body;

    protected Response() {
        // Used by wrapper
    }

    Response(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }


    /**
     * Sets the status code for the
     *
     * @param statusCode the status code
     */
    public void status(int statusCode) {
        if(httpExchange instanceof HttpExchangeWrapper wrapper){
            wrapper.setResponseCode(statusCode);
        }
    }

    /**
     * Returns the status code
     *
     * @return the status code
     */
    public int status() {
        return httpExchange.getResponseCode();
    }

    /**
     * Sets the content type for the response
     *
     * @param contentType the content type
     */
    public void type(String contentType) {
        httpExchange.getResponseHeaders().add("Content-Type", contentType);
    }

    /**
     * Returns the content type
     *
     * @return the content type
     */
    public String type() {
        return httpExchange.getResponseHeaders().getFirst("Content-Type");
    }

    /**
     * Sets the body
     *
     * @param body the body
     */
    public void body(String body) {
        this.body = body;
    }

    /**
     * returns the body
     *
     * @return the body
     */
    public String body() {
        return this.body;
    }

    /**
     * @return the raw response object handed in by Jetty
     */
    public HttpExchange raw() {
        return httpExchange;
    }

    /**
     * Trigger a browser redirect
     *
     * @param location Where to redirect
     */
    public void redirect(String location) {
        redirect( location, 302);
    }

    /**
     * Trigger a browser redirect with specific http 3XX status code.
     *
     * @param location       Where to redirect permanently
     * @param httpStatusCode the http status code
     */
    public void redirect(String location, int httpStatusCode) {
        if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
            LOG.log(System.Logger.Level.DEBUG, "Redirecting {0} to {1}", httpStatusCode, location);
        }
        /*if(httpExchange instanceof HttpExchangeWrapper wrapper){
            wrapper.setResponseCode(httpStatusCode);
        }*/
        httpExchange.getResponseHeaders().put("Location", List.of(location));
        httpExchange.getResponseHeaders().put("Connection", List.of("close"));

        try {
            //response.sendError(httpStatusCode);
            httpExchange.sendResponseHeaders(httpStatusCode, 0);
            httpExchange.getResponseBody().flush();
            httpExchange.getResponseBody().close();
        } catch (IOException e) {
            LOG.log(System.Logger.Level.WARNING,"Exception when trying to redirect permanently", e);
        }
    }

    /**
     * Adds/Sets a response header
     *
     * @param header the header
     * @param value  the value
     */
    public void header(String header, String value) {
        httpExchange.getResponseHeaders().add(header, value);
    }

    /**
     * Adds/Sets a response header
     *
     * @param header the header
     * @param value  the value
     */
    public void header(String header, int value) {
        httpExchange.getResponseHeaders().add(header, String.valueOf(value));
    }

    /**
     * Adds/Sets a response header
     *
     * @param header the header
     * @param value  the value
     */
    public void header(String header, Date value) {
        httpExchange.getResponseHeaders().add(header, String.valueOf(value.getTime()));
    }

    /**
     * Adds/Sets a response header
     *
     * @param header the header
     * @param value  the value
     */
    public void header(String header, java.sql.Date value) {
        httpExchange.getResponseHeaders().add(header, String.valueOf(value.getTime()));
    }

    /**
     * Adds/Sets a response header
     *
     * @param header the header
     * @param value  the value
     */
    public void header(String header, Instant value) {
        httpExchange.getResponseHeaders().add(header, String.valueOf(value.toEpochMilli()));
    }

    /**
     * Adds not persistent cookie to the response.
     * Can be invoked multiple times to insert more than one cookie.
     *
     * @param name  name of the cookie
     * @param value value of the cookie
     */
    public void cookie(String name, String value) {
        cookie(name, value, -1, false);
    }

    /**
     * Adds cookie to the response. Can be invoked multiple times to insert more than one cookie.
     *
     * @param name   name of the cookie
     * @param value  value of the cookie
     * @param maxAge max age of the cookie in seconds (negative for the not persistent cookie,
     *               zero - deletes the cookie)
     */
    public void cookie(String name, String value, int maxAge) {
        cookie(name, value, maxAge, false);
    }

    /**
     * Adds cookie to the response. Can be invoked multiple times to insert more than one cookie.
     *
     * @param name    name of the cookie
     * @param value   value of the cookie
     * @param maxAge  max age of the cookie in seconds (negative for the not persistent cookie, zero - deletes the cookie)
     * @param secured if true : cookie will be secured
     */
    public void cookie(String name, String value, int maxAge, boolean secured) {
        cookie(name, value, maxAge, secured, false);
    }

    /**
     * Adds cookie to the response. Can be invoked multiple times to insert more than one cookie.
     *
     * @param name     name of the cookie
     * @param value    value of the cookie
     * @param maxAge   max age of the cookie in seconds (negative for the not persistent cookie, zero - deletes the cookie)
     * @param secured  if true : cookie will be secured
     * @param httpOnly if true: cookie will be marked as http only
     */
    public void cookie(String name, String value, int maxAge, boolean secured, boolean httpOnly) {
        cookie("", "", name, value, maxAge, secured, httpOnly);
    }

    /**
     * Adds cookie to the response. Can be invoked multiple times to insert more than one cookie.
     *
     * @param path    path of the cookie
     * @param name    name of the cookie
     * @param value   value of the cookie
     * @param maxAge  max age of the cookie in seconds (negative for the not persistent cookie, zero - deletes the cookie)
     * @param secured if true : cookie will be secured
     */
    public void cookie(String path, String name, String value, int maxAge, boolean secured) {
        cookie("", path, name, value, maxAge, secured, false);
    }

    /**
     * Adds cookie to the response. Can be invoked multiple times to insert more than one cookie.
     *
     * @param path     path of the cookie
     * @param name     name of the cookie
     * @param value    value of the cookie
     * @param maxAge   max age of the cookie in seconds (negative for the not persistent cookie, zero - deletes the cookie)
     * @param secured  if true : cookie will be secured
     * @param httpOnly if true: cookie will be marked as http only
     */
    public void cookie(String path, String name, String value, int maxAge, boolean secured, boolean httpOnly) {
        cookie("", path, name, value, maxAge, secured, httpOnly);
    }

    /**
     * Adds cookie to the response. Can be invoked multiple times to insert more than one cookie.
     *
     * @param domain   domain of the cookie
     * @param path     path of the cookie
     * @param name     name of the cookie
     * @param value    value of the cookie
     * @param maxAge   max age of the cookie in seconds (negative for the not persistent cookie, zero - deletes the cookie)
     * @param secured  if true : cookie will be secured
     * @param httpOnly if true: cookie will be marked as http only
     */
    public void cookie(String domain, String path, String name, String value, int maxAge, boolean secured, boolean httpOnly) {

        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setDomain(domain);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(secured);
        cookie.setHttpOnly(httpOnly);
        addCookie(cookie);
    }

    /**
     * Removes the cookie.
     *
     * @param name name of the cookie
     */
    public void removeCookie(String name) {
        removeCookie(null, name);
    }

    /**
     * Removes the cookie with given path and name.
     *
     * @param path path of the cookie
     * @param name name of the cookie
     */
    public void removeCookie(String path, String name) {
        
        Cookie cookie = new Cookie(name, "");
        cookie.setPath(path);
        cookie.setMaxAge(0);
        addCookie(cookie);
    }

    private void addCookie(Cookie cookie) {
        String cookieString = CookieProcessor.getInstance().generateHeader(cookie);
        // if we reached here, no exception, cookie is valid
        header("Set-Cookie", cookieString);
    }
}
