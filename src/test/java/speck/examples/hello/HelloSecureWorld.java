package speck.examples.hello;

import static speck.Speck.get;
import static speck.Speck.secure;

/**
 * You'll need to provide a JKS keystore as arg 0 and its password as arg 1.
 */
public class HelloSecureWorld {
    public static void main(String[] args) {

        secure(args[0], args[1], null, null);
        get("/hello", (request, response) -> "Hello Secure World!");

    }
}
