package speck;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class CookieProcessor {

    protected static final String ANCIENT_DATE;
    private static final CookieProcessor INSTANCE = new CookieProcessor();
    private static final String COOKIE_DATE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";

    protected static final ThreadLocal<DateFormat> COOKIE_DATE_FORMAT = ThreadLocal.withInitial(() -> {
        DateFormat df = new SimpleDateFormat(COOKIE_DATE_PATTERN, Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df;
    });
    private static final BitSet domainValid = new BitSet(128);

    static {
        ANCIENT_DATE = COOKIE_DATE_FORMAT.get().format(new Date(10000));
    }

    private SameSiteCookies sameSiteCookies = SameSiteCookies.UNSET;

    public static CookieProcessor getInstance() {
        return INSTANCE;
    }

    public static boolean isToken(String s) {
        if (s == null) {
            return false;
        }
        if (s.isEmpty()) {
            return false;
        }
        for (char c : s.toCharArray()) {
            if (!isToken(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isToken(int c) {
        if (c > 128) {
            //by definition
            return false;
        }
        if (c < 32 || c == 127) {
            //control chars
            return false;
        }
        if (c == '(' || c == ')' || c == '<' || c == '>' || c == '@' || c == ',' || c == ';' || c == ':' ||
            c == '\\' || c == '\"' || c == '/' || c == '[' || c == ']' || c == '?' || c == '=' || c == '{' ||
            c == '}' || c == ' ' || c == '\t') {
            //is separator
            return false;
        }
        return true;
    }

    public SameSiteCookies getSameSiteCookies() {
        return sameSiteCookies;
    }

    public void setSameSiteCookies(String sameSiteCookies) {
        this.sameSiteCookies = SameSiteCookies.fromString(sameSiteCookies);
    }

    public String generateHeader(Cookie cookie) {

        // Can't use StringBuilder due to DateFormat
        StringBuffer header = new StringBuffer();

        /*
         * TODO: Name validation takes place in Cookie and cannot be configured per Context. Moving it to here would
         * allow per Context config but delay validation until the header is generated. However, the spec requires an
         * IllegalArgumentException on Cookie generation.
         */
        header.append(cookie.getName());
        header.append('=');
        String value = cookie.getValue();
        if (value != null && value.length() > 0) {
            validateCookieValue(value);
            header.append(value);
        }

        // RFC 6265 prefers Max-Age to Expires but... (see below)
        int maxAge = cookie.getMaxAge();
        if (maxAge > -1) {
            // Negative Max-Age is equivalent to no Max-Age
            header.append("; Max-Age=");
            header.append(maxAge);

            // Microsoft IE and Microsoft Edge don't understand Max-Age so send
            // expires as well. Without this, persistent cookies fail with those
            // browsers. See http://tomcat.markmail.org/thread/g6sipbofsjossacn

            // Wdy, DD-Mon-YY HH:MM:SS GMT ( Expires Netscape format )
            header.append("; Expires=");
            // To expire immediately we need to set the time in past
            if (maxAge == 0) {
                header.append(ANCIENT_DATE);
            } else {
                COOKIE_DATE_FORMAT.get().format(new Date(System.currentTimeMillis() + maxAge * 1000L), header,
                    new FieldPosition(0));
            }
        }

        String domain = cookie.getDomain();
        if (domain != null && domain.length() > 0) {
            validateDomain(domain);
            header.append("; Domain=");
            header.append(domain);
        }

        String path = cookie.getPath();
        if (path != null && path.length() > 0) {
            validatePath(path);
            header.append("; Path=");
            header.append(path);
        }

        if (cookie.getSecure()) {
            header.append("; Secure");
        }

        if (cookie.isHttpOnly()) {
            header.append("; HttpOnly");
        }

        String cookieSameSite = cookie.getAttribute("SameSite");
        if (cookieSameSite == null) {
            // Use processor config
            SameSiteCookies sameSiteCookiesValue = getSameSiteCookies();
            if (!sameSiteCookiesValue.equals(SameSiteCookies.UNSET)) {
                header.append("; SameSite=");
                header.append(sameSiteCookiesValue.getValue());
            }
        } else {
            // Use explicit config
            header.append("; SameSite=");
            header.append(cookieSameSite);
        }


        // Add the remaining attributes
        for (Map.Entry<String, String> entry : cookie.getAttributes().entrySet()) {
            switch (entry.getKey()) {
                case "Comment":
                case "Domain":
                case "Max-Age":
                case "Path":
                case "Secure":
                case "HttpOnly":
                case "SameSite":
                    // Handled above so NO-OP
                    break;
                default: {
                    validateAttribute(entry.getKey(), entry.getValue());
                    header.append("; ");
                    header.append(entry.getKey());
                    header.append('=');
                    header.append(entry.getValue());
                }
            }
        }

        return header.toString();
    }

    private void validateCookieValue(String value) {
        int start = 0;
        int end = value.length();
        boolean quoted = false;

        if (end > 1 && value.charAt(0) == '"' && value.charAt(end - 1) == '"') {
            quoted = true;
        }

        char[] chars = value.toCharArray();
        for (int i = start; i < end; i++) {
            if (quoted && (i == start || i == end - 1)) {
                continue;
            }
            char c = chars[i];
            if (c < 0x21 || c == 0x22 || c == 0x2c || c == 0x3b || c == 0x5c || c == 0x7f) {
                throw new IllegalArgumentException("An invalid character [" + Integer.toString(c) + "] was present in the Cookie value");
            }
        }
    }

    private void validateDomain(String domain) {
        int i = 0;
        int prev = -1;
        int cur = -1;
        char[] chars = domain.toCharArray();
        while (i < chars.length) {
            prev = cur;
            cur = chars[i];
            if (!domainValid.get(cur)) {
                throw new IllegalArgumentException("An invalid domain [" + domain + "] was specified for this cookie");
            }
            // labels must start with a letter or number
            if ((prev == '.' || prev == -1) && (cur == '.' || cur == '-')) {
                throw new IllegalArgumentException("An invalid domain [" + domain + "] was specified for this cookie");
            }
            // labels must end with a letter or number
            if (prev == '-' && cur == '.') {
                throw new IllegalArgumentException("An invalid domain [" + domain + "] was specified for this cookie");
            }
            i++;
        }
        // domain must end with a label
        if (cur == '.' || cur == '-') {
            throw new IllegalArgumentException("An invalid domain [" + domain + "] was specified for this cookie");
        }
    }

    private void validatePath(String path) {
        char[] chars = path.toCharArray();

        for (char ch : chars) {
            if (ch < 0x20 || ch > 0x7E || ch == ';') {
                throw new IllegalArgumentException("An invalid path [" + path + "] was specified for this cookie");
            }
        }
    }

    private void validateAttribute(String name, String value) {
        if (!isToken(name)) {
            throw new IllegalArgumentException("An invalid attribute name [" + name + "] was specified for this cookie");
        }

        char[] chars = value.toCharArray();
        for (char ch : chars) {
            if (ch < 0x20 || ch > 0x7E || ch == ';') {
                throw new IllegalArgumentException("An invalid attribute value [" + value + "] was specified for this cookie attribute [" + name + "]");
            }
        }
    }


}
