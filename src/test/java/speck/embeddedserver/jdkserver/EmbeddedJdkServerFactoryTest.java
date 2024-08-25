package speck.embeddedserver.jdkserver;


import org.junit.jupiter.api.Test;

import speck.ExceptionMapper;
import speck.embeddedserver.EmbeddedServer;
import speck.route.Routes;
import speck.staticfiles.StaticFilesConfiguration;



import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class EmbeddedJdkServerFactoryTest {

    private EmbeddedServer embeddedServer;
/*
    @Test
    public void create() throws Exception {
        final JdkServerFactory jettyServerFactory = mock(JdkServerFactory.class);
        final StaticFilesConfiguration staticFilesConfiguration = mock(StaticFilesConfiguration.class);
        final ExceptionMapper exceptionMapper = mock(ExceptionMapper.class);
        final Routes routes = mock(Routes.class);

        Server server = new Server();
        when(jettyServerFactory.create(100, 10, 10000)).thenReturn(server);

        final EmbeddedJdkServerFactory embeddedJettyFactory = new EmbeddedJdkServerFactory(jettyServerFactory);
        embeddedServer = embeddedJettyFactory.create(routes, staticFilesConfiguration, exceptionMapper, false);

        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 6757, null, 100, 10, 10000);

        verify(jettyServerFactory, times(1)).create(100, 10, 10000);
        verifyNoMoreInteractions(jettyServerFactory);
        assertTrue(((JettyHandler) server.getHandler()).getSessionCookieConfig().isHttpOnly());
    }

    @Test
    public void create_withThreadPool() throws Exception {
        final QueuedThreadPool threadPool = new QueuedThreadPool(100);
        final JdkServerFactory jettyServerFactory = mock(JdkServerFactory.class);
        final StaticFilesConfiguration staticFilesConfiguration = mock(StaticFilesConfiguration.class);
        final ExceptionMapper exceptionMapper = mock(ExceptionMapper.class);
        final Routes routes = mock(Routes.class);

        when(jettyServerFactory.create(threadPool)).thenReturn(new Server(threadPool));

        final EmbeddedJdkServerFactory embeddedJettyFactory = new EmbeddedJdkServerFactory(jettyServerFactory).withExecutor(threadPool);
        embeddedServer = embeddedJettyFactory.create(routes, staticFilesConfiguration, exceptionMapper, false);

        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 6758, null, 0, 0, 0);

        verify(jettyServerFactory, times(1)).create(threadPool);
        verifyNoMoreInteractions(jettyServerFactory);
    }

    @Test
    public void create_withNullThreadPool() throws Exception {
        final JdkServerFactory jettyServerFactory = mock(JdkServerFactory.class);
        final StaticFilesConfiguration staticFilesConfiguration = mock(StaticFilesConfiguration.class);
        final ExceptionMapper exceptionMapper = mock(ExceptionMapper.class);
        final Routes routes = mock(Routes.class);

        when(jettyServerFactory.create(100, 10, 10000)).thenReturn(new Server());

        final EmbeddedJdkServerFactory embeddedJettyFactory = new EmbeddedJdkServerFactory(jettyServerFactory).withExecutor(null);
        embeddedServer = embeddedJettyFactory.create(routes, staticFilesConfiguration, exceptionMapper, false);

        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 6759, null, 100, 10, 10000);

        verify(jettyServerFactory, times(1)).create(100, 10, 10000);
        verifyNoMoreInteractions(jettyServerFactory);
    }

    @Test
    public void create_withoutHttpOnly() throws Exception {
        final JdkServerFactory jettyServerFactory = mock(JdkServerFactory.class);
        final StaticFilesConfiguration staticFilesConfiguration = mock(StaticFilesConfiguration.class);
        final Routes routes = mock(Routes.class);

        Server server = new Server();
        when(jettyServerFactory.create(100, 10, 10000)).thenReturn(server);

        final EmbeddedJdkServerFactory embeddedJettyFactory = new EmbeddedJdkServerFactory(jettyServerFactory).withHttpOnly(false);
        embeddedServer = embeddedJettyFactory.create(routes, staticFilesConfiguration, false);
        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 6759, null, 100, 10, 10000);

        assertFalse(((JettyHandler) server.getHandler()).getSessionCookieConfig().isHttpOnly());
    }

    @AfterEach
    public void tearDown() {
        if (embeddedServer != null) {
            embeddedServer.extinguish();
        }
    }

 */
}
