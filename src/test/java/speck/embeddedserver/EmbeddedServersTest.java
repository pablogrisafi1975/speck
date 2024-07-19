package speck.embeddedserver;

import java.io.File;


import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import speck.Speck;
import speck.embeddedserver.jdkserver.EmbeddedJdkServerFactory;
import speck.embeddedserver.jdkserver.JdkServerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmbeddedServersTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
/*
    @Test
    public void testAddAndCreate_whenCreate_createsCustomServer() throws Exception {
        // Create custom Server
        Server server = new Server();
        File requestLogDir = temporaryFolder.newFolder();
        File requestLogFile = new File(requestLogDir, "request.log");
        server.setRequestLog(new NCSARequestLog(requestLogFile.getAbsolutePath()));
        JdkServerFactory serverFactory = mock(JdkServerFactory.class);
        when(serverFactory.create(0, 0, 0)).thenReturn(server);

        String id = "custom";

        // Register custom server
        EmbeddedServers.add(id, new EmbeddedJdkServerFactory(serverFactory));
        EmbeddedServer embeddedServer = EmbeddedServers.create(id, null, null, null, false);
        assertNotNull(embeddedServer);

        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 0, null, 0, 0, 0);

        assertTrue(requestLogFile.exists());
        embeddedServer.extinguish();
        verify(serverFactory).create(0, 0, 0);
    }

    @Test
    public void testAdd_whenConfigureRoutes_createsCustomServer() throws Exception {
        File requestLogDir = temporaryFolder.newFolder();
        File requestLogFile = new File(requestLogDir, "request.log");
        // Register custom server
        EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, new EmbeddedJdkServerFactory(new JdkServerFactory() {
            @Override
            public Server create(int maxThreads, int minThreads, int threadTimeoutMillis) {
                Server server = new Server();
                server.setRequestLog(new NCSARequestLog(requestLogFile.getAbsolutePath()));
                return server;
            }

            @Override
            public Server create(ThreadPool threadPool) {
                return null;
            }
        }));
        Speck.get("/", (request, response) -> "OK");
        Speck.awaitInitialization();

        assertTrue(requestLogFile.exists());
    }
*/
    @AfterClass
    public static void tearDown() {
        Speck.stop();
    }

}
