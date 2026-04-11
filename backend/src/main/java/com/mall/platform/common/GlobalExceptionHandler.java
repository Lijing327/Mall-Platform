package com.mall.platform.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理请求体参数校验异常。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理 URL 或 Query 参数校验异常。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException exception) {
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), exception.getMessage());
    }

    /**
     * 处理业务异常。
     */
    @ExceptionHandler(BizException.class)
    public Result<Object> handleBizException(BizException exception) {
        return Result.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 兜底异常处理，避免异常堆栈直接返回给前端。
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception exception) {
        return Result.fail(ResultCode.INTERNAL_ERROR.getCode(), extractDeepestMessage(exception));
    }

    /**
     * 从异常链中取出最有用的提示（优先 JDBC/SQL 根因），避免只显示 MyBatisSystemException 外壳。
     */
    private static String extractDeepestMessage(Throwable error) {
        Throwable cursor = error;
        String firstNonTrivial = null;
        while (cursor != null) {
            if (cursor instanceof java.sql.SQLException) {
                String m = cursor.getMessage();
                if (m != null && !m.isBlank()) {
                    return m;
                }
            }
            String m = cursor.getMessage();
            if (m != null && !m.isBlank() && !m.equals(cursor.getClass().getName())) {
                if (firstNonTrivial == null) {
                    firstNonTrivial = m;
                }
            }
            Throwable next = cursor.getCause();
            if (next == null || next == cursor) {
                break;
            }
            cursor = next;
        }
        if (firstNonTrivial != null) {
            return firstNonTrivial;
        }
        return error.getClass().getSimpleName();
    }
}
