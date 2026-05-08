package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.service.task.PreflightCheckException;
import cn.org.hentai.simulator.service.task.PreflightCheckResult;
import cn.org.hentai.simulator.web.exception.ValidationException;
import cn.org.hentai.simulator.web.vo.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ApiExceptionHandlerTest
{
    @Test
    void validationFailuresReturnBadRequestErrorResponse()
    {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(new ValidationException("直接鉴权设备必须填写鉴权码"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_FAILED", response.getBody().getCode());
        assertEquals("直接鉴权设备必须填写鉴权码", response.getBody().getMessage());
    }

    @Test
    void preflightFailuresReturnPrecheckFailedWithPreflightDetails()
    {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        PreflightCheckResult preflight = new PreflightCheckResult();
        preflight.fail("文件描述符不足");

        ResponseEntity<ErrorResponse> response = handler.handlePreflightCheckException(new PreflightCheckException(preflight));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("PRECHECK_FAILED", response.getBody().getCode());
        assertEquals("文件描述符不足", response.getBody().getMessage());
        assertSame(preflight, response.getBody().getDetails().get("preflight"));
    }

    @Test
    void unexpectedFailuresReturnInternalError()
    {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleException(new RuntimeException("数据库连接失败"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("服务端异常", response.getBody().getMessage());
    }

    @Test
    void numberFormatFailuresReturnBadRequest()
    {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(new NumberFormatException("For input string: \"abc\""));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().getCode());
    }
}
