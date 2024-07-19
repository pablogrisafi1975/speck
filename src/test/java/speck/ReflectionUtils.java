package speck;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class ReflectionUtils {
    public static Object read(Object obj, String fieldName) {
        // Create Field object
        Field privateField = null;
        try {
            privateField = findDeclaredField(obj, fieldName);
            // Set the accessibility as true
            privateField.setAccessible(true);

            // Store the value of private field in variable
            return privateField.get(obj);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field findDeclaredField(Object obj, String fieldName) throws NoSuchFieldException {
        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            Field[] declaredFields = clazz.getDeclaredFields();
            Optional<Field> found = Arrays.stream(declaredFields).filter(f -> f.getName().equals(fieldName)).findFirst();
            if(found.isPresent()){
                return found.get();
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchFieldException("Can not find field " + fieldName + " in class " + clazz);
    }

    public static Object readStatic(Class<?> clazz, String fieldName) {
        // Create Field object
        Field privateField;
        try {
            privateField = clazz.getDeclaredField(fieldName);
            // Set the accessibility as true
            privateField.setAccessible(true);

            // Store the value of private field in variable
            return privateField.get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(Object obj, String fieldName, Object value) {
        // Create Field object
        Field privateField;
        try {
            privateField = findDeclaredField(obj, fieldName);
            // Set the accessibility as true
            privateField.setAccessible(true);

            // Store the value of private field in variable
            privateField.set(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeStatic(Class<?> clazz, String fieldName, Object value) {
        // Create Field object
        Field privateField;
        try {
            privateField = clazz.getDeclaredField(fieldName);
            // Set the accessibility as true
            privateField.setAccessible(true);

            // Store the value of private field in variable
            privateField.set(null, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
