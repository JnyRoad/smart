package com.tce.smart.common.core.exception;

import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import lombok.Data;

@Data
public class SmartException extends RuntimeException {

    /**
     * 异常码
     */
    private Integer code;

    public SmartException(String message) {
        super(message);
    }

    public SmartException(ExceptionEnum exceptionEnum){
        super(exceptionEnum.getDesc());
        this.code = exceptionEnum.getCode();
    }

    public SmartException(Integer code, String message){
        super(message);
        this.code = code;
    }

}
