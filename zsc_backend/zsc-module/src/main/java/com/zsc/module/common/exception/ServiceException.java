package com.zsc.module.common.exception;

/**
 * 业务异常类，继承框架 ServiceException 以确保被 GlobalExceptionHandler 统一处理
 *
 * @author zsc
 */
public class ServiceException extends com.zsc.common.exception.ServiceException {

    private static final long serialVersionUID = 1L;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException() {
        super();
    }
}
