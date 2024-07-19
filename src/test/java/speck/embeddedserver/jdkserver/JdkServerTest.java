package speck.embeddedserver.jdkserver;


import org.junit.Test;
import speck.ReflectionUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JdkServerTest {
    /*@Test
    public void testCreateServer_useDefaults() {
        Server server = new JdkServer().create(0, 0, 0);

        QueuedThreadPool threadPool = (QueuedThreadPool) server.getThreadPool();

        int minThreads = (int)ReflectionUtils.read(threadPool, "_minThreads");
        int maxThreads = (int)ReflectionUtils.read(threadPool, "_maxThreads");
        int idleTimeout = (int)ReflectionUtils.read(threadPool, "_idleTimeout");

        assertEquals("Server thread pool default minThreads should be 8", 8, minThreads);
        assertEquals("Server thread pool default maxThreads should be 200", 200, maxThreads);
        assertEquals("Server thread pool default idleTimeout should be 60000", 60000, idleTimeout);


    }

    @Test
    public void testCreateServer_whenNonDefaultMaxThreadsOnly_thenUseDefaultMinThreadsAndTimeout() {
        Server server = new JdkServer().create(9, 0, 0);

        QueuedThreadPool threadPool = (QueuedThreadPool) server.getThreadPool();

        int minThreads = (int)ReflectionUtils.read(threadPool, "_minThreads");
        int maxThreads = (int)ReflectionUtils.read(threadPool, "_maxThreads");
        int idleTimeout = (int)ReflectionUtils.read(threadPool, "_idleTimeout");

        assertEquals("Server thread pool default minThreads should be 8", 8, minThreads);
        assertEquals("Server thread pool default maxThreads should be the same as specified", 9, maxThreads);
        assertEquals("Server thread pool default idleTimeout should be 60000", 60000, idleTimeout);

    }

    @Test
    public void testCreateServer_whenNonDefaultMaxThreads_isLessThanDefaultMinThreads() {
        try {
            new JdkServer().create(2, 0, 0);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
            assertEquals("max threads (2) less than min threads (8)", expected.getMessage());
        }
    }*/
}
