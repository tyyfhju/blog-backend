package com.zjy.blog.exception;

/**
 * 可安全返回给调用方的业务异常。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
