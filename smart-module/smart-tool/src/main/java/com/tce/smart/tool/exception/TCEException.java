package com.tce.smart.tool.exception;

import com.tce.smart.tool.enums.ExceptionTypeEnum;

/**
 * 自定义的异常
 *
 * @author WangJinbo
 * @Date 2017/12/28 下午10:32
 */
public class TCEException extends RuntimeException {

    private Integer code;

    private String message;

    public TCEException(ExceptionTypeEnum exceptionEnum) {
        this.code = exceptionEnum.getCode();
        this.message = exceptionEnum.getMessage();
    }

    public TCEException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public TCEException(String message) {
        this.code = ExceptionTypeEnum.SERVER_ERROR.getCode();
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
