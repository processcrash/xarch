package com.xarch.starter.core.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SelectIdsDTO}.
 *
 * <p>Validates getter/setter behavior, null safety, and Bean Validation
 * constraint enforcement on the {@code ids} list.</p>
 */
@DisplayName("SelectIdsDTO Tests")
class SelectIdsDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        if (factory != null) {
            factory.close();
        }
    }

    @Nested
    @DisplayName("Getter / Setter")
    class GetterSetter {

        @Test
        @DisplayName("Default ids is null")
        void defaultIds_isNull() {
            SelectIdsDTO dto = new SelectIdsDTO();
            assertThat(dto.getIds()).isNull();
        }

        @Test
        @DisplayName("setIds / getIds round-trip the same list")
        void setIds_thenGet_returnsSameList() {
            SelectIdsDTO dto = new SelectIdsDTO();
            List<Long> ids = Arrays.asList(1L, 2L, 3L);

            dto.setIds(ids);

            assertThat(dto.getIds()).containsExactly(1L, 2L, 3L);
        }
    }

    @Nested
    @DisplayName("Null Safety")
    class NullSafety {

        @Test
        @DisplayName("Null ids is allowed (no implicit validation at this layer)")
        void nullIds_isAccepted() {
            SelectIdsDTO dto = new SelectIdsDTO();
            dto.setIds(null);

            assertThat(dto.getIds()).isNull();
        }
    }

    @Nested
    @DisplayName("Empty List Behavior")
    class EmptyListBehavior {

        @Test
        @DisplayName("Empty list is stored as-is")
        void emptyList_isStoredAsIs() {
            SelectIdsDTO dto = new SelectIdsDTO();
            dto.setIds(Collections.emptyList());

            assertThat(dto.getIds()).isEmpty();
        }

        @Test
        @DisplayName("Validation rejects empty list with NotEmpty constraint")
        void validation_rejectsEmptyList() {
            // Arrange
            SelectIdsDTO dto = new SelectIdsDTO();
            dto.setIds(Collections.emptyList());

            // Act
            Set<ConstraintViolation<SelectIdsDTO>> violations = validator.validate(dto);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anySatisfy(v -> assertThat(v.getMessageTemplate()).contains("IDs cannot be empty"));
        }

        @Test
        @DisplayName("Validation rejects null list with NotEmpty constraint")
        void validation_rejectsNullList() {
            // Arrange
            SelectIdsDTO dto = new SelectIdsDTO();

            // Act
            Set<ConstraintViolation<SelectIdsDTO>> violations = validator.validate(dto);

            // Assert
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("Validation passes when ids list is populated")
        void validation_passesForPopulatedList() {
            // Arrange
            SelectIdsDTO dto = new SelectIdsDTO();
            dto.setIds(List.of(10L, 20L));

            // Act
            Set<ConstraintViolation<SelectIdsDTO>> violations = validator.validate(dto);

            // Assert
            assertThat(violations).isEmpty();
        }
    }
}