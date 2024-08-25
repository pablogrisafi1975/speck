package speck;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;




public class Base64Test {

    //CS304 manually Issue link:https://github.com/perwendel/speck/issues/1061

    @Test
    public final void test_encode() {
        String in = "hello";
        String encode = Base64.encode(in);
        assertFalse(in.equals(encode));
    }

    //CS304 manually Issue link:https://github.com/perwendel/speck/issues/1061

    @Test
    public final void test_decode() {
        String in = "hello";
        String encode = Base64.encode(in);
        String decode = Base64.decode(encode);

        assertTrue(in.equals(decode));
    }

}
