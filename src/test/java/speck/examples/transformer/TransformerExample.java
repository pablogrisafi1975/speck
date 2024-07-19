package speck.examples.transformer;

import static speck.Speck.get;

public class TransformerExample {

    public static void main(String args[]) {
        get("/hello", "application/json", (request, response) -> new MyMessage("Hello World"), new JsonTransformer());
    }

}
