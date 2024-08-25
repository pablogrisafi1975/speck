package speck.serialization;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputStreamSerializerTest {

    private InputStreamSerializer serializer = new InputStreamSerializer();

    @Test
    public void testProcess_copiesData() throws IOException {
        byte[] bytes = "Hello, Speck!".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        serializer.process(output, input);

        assertArrayEquals(bytes, output.toByteArray());
    }

    @Test
    public void testProcess_closesStream() throws IOException {
        MockInputStream input = new MockInputStream(new ByteArrayInputStream(new byte[0]));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        serializer.process(output, input);

        assertTrue(input.closed, "Expected stream to be closed");
    }

    private static class MockInputStream extends FilterInputStream {

        boolean closed = false;

        private MockInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }
}
