package speck.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CollectionUtilsTest {

    @Test
    public void testIsEmpty_whenCollectionIsEmpty_thenReturnTrue() {

        Collection<Object> testCollection = new ArrayList<>();

        assertTrue( CollectionUtils.isEmpty(testCollection), "Should return true because collection is empty");

    }

    @Test
    public void testIsEmpty_whenCollectionIsNotEmpty_thenReturnFalse() {

        Collection<Integer> testCollection = new ArrayList<>();
        testCollection.add(1);
        testCollection.add(2);

        assertFalse(CollectionUtils.isEmpty(testCollection), "Should return false because collection is not empty");

    }

    @Test
    public void testIsEmpty_whenCollectionIsNull_thenReturnTrue() {

        Collection<Integer> testCollection = null;

        assertTrue( CollectionUtils.isEmpty(testCollection), "Should return true because collection is null");

    }


}
