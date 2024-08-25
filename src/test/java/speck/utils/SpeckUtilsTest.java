package speck.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SpeckUtilsTest {

    @Test
    public void testConvertRouteToList() {

        List<String> expected = Arrays.asList("api", "person", ":id");

        List<String> actual = SpeckUtils.convertRouteToList("/api/person/:id");

        assertEquals(expected,actual, "Should return route as a list of individual elements that path is made of");

    }

    @Test
    public void testIsParam_whenParameterFormattedAsParam() {

        assertTrue(SpeckUtils.isParam(":param"), "Should return true because parameter follows convention of a parameter (:paramname)");

    }

    @Test
    public void testIsParam_whenParameterNotFormattedAsParam() {

        assertFalse(SpeckUtils.isParam(".param"), "Should return false because parameter does not follows convention of a parameter (:paramname)"
                );

    }


    @Test
    public void testIsSplat_whenParameterIsASplat() throws Exception {

        assertTrue(SpeckUtils.isSplat("*"), "Should return true because parameter is a splat (*)");

    }

    @Test
    public void testIsSplat_whenParameterIsNotASplat() throws Exception {

        assertFalse(SpeckUtils.isSplat("!"), "Should return true because parameter is not a splat (*)");

    }
}
