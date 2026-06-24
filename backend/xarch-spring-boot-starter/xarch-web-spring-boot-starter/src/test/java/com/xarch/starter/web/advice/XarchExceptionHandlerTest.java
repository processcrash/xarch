package com.xarch.starter.web.advice;

import com.xarch.starter.core.exception.BusinessException;
import com.xarch.starter.core.exception.XarchException;
import com.xarch.starter.core.result.ApiResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XarchExceptionHandler}.
 *
 * <p>Exercises each exception branch and verifies the resulting {@link ApiResult}
 * (code + message) is built from the exception payload.</p>
 */
@DisplayName("XarchExceptionHandler Tests")
class XarchExceptionHandlerTest {

    private XarchExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new XarchExceptionHandler();
    }

    @Nested
    @DisplayName("XarchException Branch")
    class XarchExceptionBranch {

        @Test
        @DisplayName("handleXarchException returns code/message from the exception")
        void handleXarchException_returnsCodeAndMessage() {
            XarchException exception = new XarchException("9999", "kaboom");

            ApiResult<Void> result = handler.handleXarchException(exception);

            assertThat(result.getCode()).isEqualTo("9999");
            assertThat(result.getMessage()).isEqualTo("kaboom");
        }
    }

    @Nested
    @DisplayName("BusinessException Branch")
    class BusinessExceptionBranch {

        @Test
        @DisplayName("handleBusinessException returns code/message from the exception")
        void handleBusinessException_returnsCodeAndMessage() {
            BusinessException exception = new BusinessException("4100", "Conflict");

            ApiResult<Void> result = handler.handleBusinessException(exception);

            assertThat(result.getCode()).isEqualTo("4100");
            assertThat(result.getMessage()).isEqualTo("Conflict");
        }
    }

    @Nested
    @DisplayName("Validation Branches")
    class ValidationBranches {

        @Test
        @DisplayName("handleConstraintViolationException returns 4000 with validation message")
        void handleConstraintViolationException_returnsBadRequestCode() {
            @SuppressWarnings("unchecked")
            ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
            Path path = mock(Path.class);
            when(path.toString()).thenReturn("field");
            when(violation.getPropertyPath()).thenReturn(path);
            when(violation.getMessage()).thenReturn("must not be blank");
            Set<ConstraintViolation<Object>> violations = Collections.singleton(violation);

            ConstraintViolationException exception = new ConstraintViolationException("violations", violations);

            ApiResult<Void> result = handler.handleConstraintViolationException(exception);

            assertThat(result.getCode()).isEqualTo("4000");
            assertThat(result.getMessage()).contains("Validation failed");
            assertThat(result.getMessage()).contains("must not be blank");
        }

        @Test
        @DisplayName("handleValidException returns the field-error default message")
        void handleValidException_returnsFieldErrorMessage() {
            // Arrange: a binding result with a single field error
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
            bindingResult.addError(new FieldError("target", "username", "username is required"));

            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

            // Act
            ApiResult<Void> result = handler.handleValidException(exception);

            // Assert
            assertThat(result.getCode()).isEqualTo("4000");
            assertThat(result.getMessage()).isEqualTo("username is required");
        }
    }

    @Nested
    @DisplayName("Generic Exception Branch")
    class GenericExceptionBranch {

        @Test
        @DisplayName("handleException returns 5000 with internal server error message")
        void handleException_returnsInternalServerError() {
            ApiResult<Void> result = handler.handleException(new RuntimeException("anything"));

            assertThat(result.getCode()).isEqualTo("5000");
            assertThat(result.getMessage()).isEqualTo("Internal server error");
        }
    }
}