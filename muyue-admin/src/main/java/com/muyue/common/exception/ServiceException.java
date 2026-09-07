package com.muyue.common.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author muyue
 */
@Getter
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final Integer code;

    /** 错误提示 */
    private final String message;

    public ServiceException(String message) {
        this(message, null);
    }

    public ServiceException(String message, Integer code) {
        super(message);
        this.message = message;
        this.code = code;
    }
}
