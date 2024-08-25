package speck.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import speck.utils.ResourceUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ResourceUtilsTest {

    @Test
    public void testGetFile_whenURLProtocolIsNotFile_thenThrowFileNotFoundException() {
        var thrown = Assertions.assertThrows(FileNotFoundException.class, () -> {
            URL url = new URI("http://example.com/").toURL();
            ResourceUtils.getFile(url, "My File Path");
        });
        assertEquals("My File Path cannot be resolved to absolute file path " +
            "because it does not reside in the file system: http://example.com/", thrown.getMessage());
    }

    @Test
    public void testGetFile_whenURLProtocolIsFile_thenReturnFileObject() throws
                                                                         MalformedURLException,
                                                                         FileNotFoundException,
                                                                         URISyntaxException {
        //given
        URL url = new URI("file://public/file.txt").toURL();
        File file = ResourceUtils.getFile(url, "Some description");

        //then
        assertEquals(file, new File(ResourceUtils.toURI(url).getSchemeSpecificPart()), "Should be equals because URL protocol is file");
    }

}
