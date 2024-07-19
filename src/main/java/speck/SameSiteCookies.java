package speck;

public enum SameSiteCookies {
    /**
     * Don't set the SameSite cookie attribute.
     */
    UNSET("Unset"),

    /**
     * Cookie is always sent in cross-site requests.
     */
    NONE("None"),

    /**
     * Cookie is only sent on same-site requests and cross-site top level navigation GET requests
     */
    LAX("Lax"),

    /**
     * Prevents the cookie from being sent by the browser in all cross-site requests
     */
    STRICT("Strict");

    private final String value;

    SameSiteCookies(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SameSiteCookies fromString(String value) {
        for (SameSiteCookies sameSiteCookies : values()) {
            if (sameSiteCookies.getValue().equalsIgnoreCase(value)) {
                return sameSiteCookies;
            }
        }

        throw new IllegalStateException("Unknown setting [" + value + "], must be one of: unset, none, lax, strict. Default value is unset.");
    }
}
