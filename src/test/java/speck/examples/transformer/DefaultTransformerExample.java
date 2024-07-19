package speck.examples.transformer;

import speck.ResponseTransformer;

import static speck.Speck.get;
import static speck.Speck.defaultResponseTransformer;

public class DefaultTransformerExample {

    public static void main(String[] args) {

        defaultResponseTransformer(json);

        get("/hello", "application/json", (request, response) -> new MyMessage("Hello World"));

        get("/hello2", "application/json", (request, response) -> new MyMessage("Hello World"), model -> "custom transformer");
    }

    private static final ResponseTransformer json = new JsonTransformer();

}
