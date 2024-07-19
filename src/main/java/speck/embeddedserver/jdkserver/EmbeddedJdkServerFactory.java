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
package speck.embeddedserver.jdkserver;


import speck.ExceptionMapper;
import speck.embeddedserver.EmbeddedServer;
import speck.embeddedserver.EmbeddedServerFactory;
import speck.http.matching.MatcherFilter;
import speck.route.Routes;
import speck.staticfiles.StaticFilesConfiguration;

import java.util.concurrent.Executor;

/**
 * Creates instances of embedded jetty containers.
 */
public class EmbeddedJdkServerFactory implements EmbeddedServerFactory {
    private final JdkServerFactory serverFactory;
    private Executor executor;
    private boolean httpOnly = true;

    public EmbeddedJdkServerFactory() {
        this.serverFactory = new JdkServer();
    }

    public EmbeddedJdkServerFactory(JdkServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }

    public EmbeddedServer create(Routes routeMatcher,
                                 StaticFilesConfiguration staticFilesConfiguration,
                                 ExceptionMapper exceptionMapper,
                                 boolean hasMultipleHandler) {
        MatcherFilter matcherFilter = new MatcherFilter(routeMatcher, staticFilesConfiguration, exceptionMapper, hasMultipleHandler);
        //matcherFilter.init(null);

        JdkServerHandler handler = new JdkServerHandler(matcherFilter);
        //handler.getSessionCookieConfig().setHttpOnly(httpOnly);
        return new EmbeddedJdkServer(serverFactory, handler).withExecutor(executor);
    }

    /**
     * Sets optional thread pool for jetty server.  This is useful for overriding the default thread pool
     * behaviour for example io.dropwizard.metrics.jetty9.InstrumentedQueuedThreadPool.
     *
     * @param threadPool thread pool
     * @return Builder pattern - returns this instance
     */
    public EmbeddedJdkServerFactory withExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public EmbeddedJdkServerFactory withHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }
}
