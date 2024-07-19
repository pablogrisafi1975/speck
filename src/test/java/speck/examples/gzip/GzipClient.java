package speck.examples.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import speck.utils.IOUtils;

/**
 * Created by Per Wendel on 2015-11-24.
 */
public class GzipClient {

    public static String getAndDecompress(String url) throws Exception {
        InputStream compressed = get(url);
        GZIPInputStream gzipInputStream = new GZIPInputStream(compressed);
        String decompressed = IOUtils.toString(gzipInputStream);
        return decompressed;
    }

    public static InputStream get(String url) throws IOException, URISyntaxException {
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
        connection.addRequestProperty("Accept-Encoding", "gzip");
        connection.connect();

        return (InputStream) connection.getInputStream();
    }

}
