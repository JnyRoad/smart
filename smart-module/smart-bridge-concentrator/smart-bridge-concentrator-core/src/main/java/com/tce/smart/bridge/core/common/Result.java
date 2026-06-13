package com.tce.smart.bridge.core.common;

import cn.hutool.http.HttpStatus;
import com.tce.smart.bridge.core.enums.SuccessEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Objects;

/**
 * 响应结果
 */
@Slf4j
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code = SuccessEnum.SUCCESS.getCode();

    private String message = "success";

    private T data;

    public static Result<Boolean> success() {
        return success(Boolean.TRUE);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setData(data);
        return result;
    }

    public static Result<Boolean> fail(Throwable e) {
        return fail(SuccessEnum.FAIL.getCode(), e.getMessage());
    }

    public static Result<Boolean> fail(String message) {
        return fail(SuccessEnum.FAIL.getCode(), message);
    }

    public static Result<Boolean> fail(Integer code, String message) {
        Result<Boolean> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result(code, message, Boolean.FALSE);
    }

    public static <T> Result<T> result(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setData(data);
        result.setMessage(message);
        return result;
    }

    public Boolean isSuccess() {
        return this.getCode().equals(SuccessEnum.SUCCESS.getCode()) || this.getCode().equals(HttpStatus.HTTP_OK);
    }

    /**
     * 获取data，不可为空
     *
     * @return
     */
    public T get() {
        return get("远程接口请求失败：" + this.getMessage());
    }

    /**
     * 获取data，不可为空
     *
     * @param message 异常提示
     * @return
     */
    public T get(String message) {
        T data = data(message);
        if (Objects.isNull(data)) {
            log.error("远程接口请求响应为空：{}", this.getMessage());
            throw new NullPointerException(message);
        }
        return data;
    }

    /**
     * 获取data，可为空
     *
     * @return
     */
    public T data() {
        return data("远程接口请求失败：" + this.getMessage());
    }

    /**
     * 获取data，可为空
     *
     * @param message 异常提示
     * @return
     */
    public T data(String message) {
        if (isSuccess()) {
            return this.data;
        }
        log.error("远程接口请求失败：{}", this.getMessage());
        throw new RuntimeException(message);
    }
}
