package speck.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SpeckUtilsTest {

    @Test
    public void testConvertRouteToList() {

        List<String> expected = Arrays.asList("api", "person", ":id");

        List<String> actual = SpeckUtils.convertRouteToList("/api/person/:id");

        assertEquals("Should return route as a list of individual elements that path is made of",
            expected,
            actual);

    }

    @Test
    public void testIsParam_whenParameterFormattedAsParam() {

        assertTrue("Should return true because parameter follows convention of a parameter (:paramname)",
                SpeckUtils.isParam(":param"));

    }

    @Test
    public void testIsParam_whenParameterNotFormattedAsParam() {

        assertFalse("Should return false because parameter does not follows convention of a parameter (:paramname)",
                SpeckUtils.isParam(".param"));

    }


    @Test
    public void testIsSplat_whenParameterIsASplat() throws Exception {

        assertTrue("Should return true because parameter is a splat (*)", SpeckUtils.isSplat("*"));

    }

    @Test
    public void testIsSplat_whenParameterIsNotASplat() throws Exception {

        assertFalse("Should return true because parameter is not a splat (*)", SpeckUtils.isSplat("!"));

    }
}
