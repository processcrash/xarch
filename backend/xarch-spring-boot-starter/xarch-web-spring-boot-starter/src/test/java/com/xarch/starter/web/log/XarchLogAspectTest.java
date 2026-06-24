package com.xarch.starter.web.log;

import com.xarch.starter.core.annotation.XarchLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XarchLogAspect}.
 *
 * <p>Verifies the aspect delegates to the underlying join point, returns the
 * value produced by the target method, and executes exactly once.</p>
 */
@DisplayName("XarchLogAspect Tests")
class XarchLogAspectTest {

    private XarchLogAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new XarchLogAspect();
    }

    @Test
    @DisplayName("around() returns the value produced by the target method")
    void around_returnsTargetResult() throws Throwable {
        // Arrange
        String expected = "ok";

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(MethodSignature.class);
        Method method = TargetService.class.getDeclaredMethod("annotatedOperation");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new TargetService());
        when(joinPoint.proceed()).thenAnswer((InvocationOnMock inv) -> expected);

        // Act
        Object result = aspect.around(joinPoint);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("around() propagates exceptions from the target method")
    void around_propagatesExceptions() throws Throwable {
        // Arrange
        RuntimeException boom = new RuntimeException("kaboom");

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(MethodSignature.class);
        Method method = TargetService.class.getDeclaredMethod("annotatedOperation");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new TargetService());
        when(joinPoint.proceed()).thenThrow(boom);

        // Act & Assert
        try {
            aspect.around(joinPoint);
        } catch (RuntimeException thrown) {
            assertThat(thrown).isSameAs(boom);
            return;
        }
        org.junit.jupiter.api.Assertions.fail("Expected exception was not propagated");
    }

    /**
     * Helper service used by the mocked join point.
     */
    @SuppressWarnings("unused")
    static class TargetService {

        @XarchLog(value = "test op", type = "TEST")
        String annotatedOperation() {
            return "ok";
        }
    }
}