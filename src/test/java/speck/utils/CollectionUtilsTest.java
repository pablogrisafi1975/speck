package speck.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CollectionUtilsTest {

    @Test
    public void testIsEmpty_whenCollectionIsEmpty_thenReturnTrue() {

        Collection<Object> testCollection = new ArrayList<>();

        assertTrue("Should return true because collection is empty", CollectionUtils.isEmpty(testCollection));

    }

    @Test
    public void testIsEmpty_whenCollectionIsNotEmpty_thenReturnFalse() {

        Collection<Integer> testCollection = new ArrayList<>();
        testCollection.add(1);
        testCollection.add(2);

        assertFalse("Should return false because collection is not empty", CollectionUtils.isEmpty(testCollection));

    }

    @Test
    public void testIsEmpty_whenCollectionIsNull_thenReturnTrue() {

        Collection<Integer> testCollection = null;

        assertTrue("Should return true because collection is null", CollectionUtils.isEmpty(testCollection));

    }


}
