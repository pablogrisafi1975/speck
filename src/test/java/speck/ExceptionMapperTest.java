package speck;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ExceptionMapperTest {


    @Test
    public void testGetInstance_whenDefaultInstanceIsNull() {
        //given

        ExceptionMapper exceptionMapper = null;
        ReflectionUtils.writeStatic(ExceptionMapper.class, "servletInstance", exceptionMapper);

        //then
        exceptionMapper = ExceptionMapper.getServletInstance();
        assertEquals(ReflectionUtils.readStatic(ExceptionMapper.class, "servletInstance"), exceptionMapper, "Should be equals because ExceptionMapper is a singleton");


    }

    @Test
    public void testGetInstance_whenDefaultInstanceIsNotNull() {
        //given
        ExceptionMapper.getServletInstance(); //initialize Singleton

        //then
        ExceptionMapper exceptionMapper = ExceptionMapper.getServletInstance();
        assertEquals(ReflectionUtils.readStatic(ExceptionMapper.class, "servletInstance"), exceptionMapper, "Should be equals because ExceptionMapper is a singleton");


    }
}
