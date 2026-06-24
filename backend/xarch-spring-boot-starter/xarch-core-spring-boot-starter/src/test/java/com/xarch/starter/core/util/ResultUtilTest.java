package com.xarch.starter.core.util;

import com.xarch.starter.core.enums.ResponseCode;
import com.xarch.starter.core.exception.BusinessException;
import com.xarch.starter.core.exception.XarchException;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ResultUtil}.
 *
 * <p>Validates the static helpers used to construct {@link ApiResult} instances
 * and to throw standardized business/xarch exceptions.</p>
 */
@DisplayName("ResultUtil Tests")
class ResultUtilTest {

    @Nested
    @DisplayName("Ok Helpers")
    class OkHelpers {

        @Test
        @DisplayName("ok() returns the success ApiResult")
        void ok_returnsSuccessResult() {
            ApiResult<Void> result = ResultUtil.ok();

            assertThat(result.getCode()).isEqualTo("0000");
            assertThat(result.getMessage()).isEqualTo("SUCCESS");
            assertThat(result.getData()).isNull();
        }

        @Test
        @DisplayName("ok(data) returns success ApiResult with payload")
        void ok_withData_returnsSuccessResult() {
            ApiResult<String> result = ResultUtil.ok("payload");

            assertThat(result.getCode()).isEqualTo("0000");
            assertThat(result.getData()).isEqualTo("payload");
        }
    }

    @Nested
    @DisplayName("Fail Helpers")
    class FailHelpers {

        @Test
        @DisplayName("fail(code, message) wraps provided code/message")
        void fail_withCodeAndMessage_returnsFailureResult() {
            ApiResult<Void> result = ResultUtil.fail("4000", "Bad Request");

            assertThat(result.getCode()).isEqualTo("4000");
            assertThat(result.getMessage()).isEqualTo("Bad Request");
        }

        @Test
        @DisplayName("fail(ResponseCode) maps enum to code/message")
        void fail_withEnum_mapsToCodeAndMessage() {
            ApiResult<Void> result = ResultUtil.fail(ResponseCode.UNAUTHORIZED);

            assertThat(result.getCode()).isEqualTo("4010");
            assertThat(result.getMessage()).isEqualTo("Unauthorized");
        }

        @Test
        @DisplayName("fail(BusinessException) reads exception code/message")
        void fail_withBusinessException_usesExceptionFields() {
            BusinessException exception = new BusinessException("4100", "Conflict");
            ApiResult<Void> result = ResultUtil.fail(exception);

            assertThat(result.getCode()).isEqualTo("4100");
            assertThat(result.getMessage()).isEqualTo("Conflict");
        }

        @Test
        @DisplayName("fail(XarchException) reads exception code/message")
        void fail_withXarchException_usesExceptionFields() {
            XarchException exception = new XarchException("9999", "Fatal");
            ApiResult<Void> result = ResultUtil.fail(exception);

            assertThat(result.getCode()).isEqualTo("9999");
            assertThat(result.getMessage()).isEqualTo("Fatal");
        }
    }

    @Nested
    @DisplayName("Page Helper")
    class PageHelper {

        @Test
        @DisplayName("page(list, total) returns a PageResult with provided values")
        void page_returnsPageResultWithValues() {
            List<Integer> items = Arrays.asList(1, 2, 3);

            PageResult<Integer> result = ResultUtil.page(items, 25L);

            assertThat(result.getList()).containsExactly(1, 2, 3);
            assertThat(result.getTotal()).isEqualTo(25L);
        }
    }

    @Nested
    @DisplayName("Throw Helpers")
    class ThrowHelpers {

        @Test
        @DisplayName("throwFail throws XarchException with provided code/message")
        void throwFail_throwsXarchException() {
            assertThatThrownBy(() -> ResultUtil.throwFail("9999", "kaboom"))
                .isInstanceOf(XarchException.class)
                .hasMessage("kaboom")
                .satisfies(t -> assertThat(((XarchException) t).getCode()).isEqualTo("9999"));
        }

        @Test
        @DisplayName("throwBizFail throws BusinessException with provided code/message")
        void throwBizFail_throwsBusinessException() {
            assertThatThrownBy(() -> ResultUtil.throwBizFail("4100", "biz kaboom"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("biz kaboom")
                .satisfies(t -> assertThat(((BusinessException) t).getCode()).isEqualTo("4100"));
        }
    }
}