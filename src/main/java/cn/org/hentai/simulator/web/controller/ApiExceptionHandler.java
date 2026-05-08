package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.service.task.PreflightCheckException;
import cn.org.hentai.simulator.web.exception.ValidationException;
import cn.org.hentai.simulator.web.vo.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler
{
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex)
    {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("VALIDATION_FAILED", ex.getMessage()));
    }

    @ExceptionHandler(PreflightCheckException.class)
    public ResponseEntity<ErrorResponse> handlePreflightCheckException(PreflightCheckException ex)
    {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("PRECHECK_FAILED", ex.getMessage(), Map.of("preflight", ex.getPreflight())));
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        NumberFormatException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex)
    {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex)
    {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "服务端异常"));
    }
}
