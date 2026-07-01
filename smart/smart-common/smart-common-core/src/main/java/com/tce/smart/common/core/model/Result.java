package com.tce.smart.common.core.model;

import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.util.StringUtils;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Objects;

/**
 * 响应信息主体
 *
 */
@Slf4j
@Builder
@ToString
@Accessors(chain = true)
@AllArgsConstructor
public class Result<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private int code = CommonConstants.SUCCESS;

	@Getter
	@Setter
	private String msg = "success";


	@Setter
	@Getter
	private T data;

	public Result() {
		super();
	}

	public Result(T data) {
		super();
		this.data = data;
	}

	public Result(T data, String msg) {
		super();
		this.data = data;
		this.msg = msg;
	}

	public Result(Throwable e) {
		super();
		this.msg = e.getMessage();
		this.code = CommonConstants.FAIL;
	}

	public Result(ExceptionType e) {
		super();
		this.msg = e.getMessage();
		this.code = CommonConstants.FAIL;
	}

	public T get() {
		return this.get(this.getMessage());
	}

	public T get(String message) {
		T data = this.data(message);
		if (Objects.isNull(data)) {
			String exception = StringUtils.isEmpty(message) ? "数据异常" : message;
			throw new NullPointerException(exception);
		}
		return data;
	}

	public T data() {
		return this.data(this.getMessage());
	}

	public T data(String message) {
		if (this.isSuccess()) {
			return this.data;
		}
		String exception = StringUtils.isEmpty(message) ? "请求异常" : message;
		throw new TCEException(exception);
	}

	public Integer getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.msg;
	}

	public static <T> Result success(T data) {
		Result result = new Result();
		result.setData(data);
		return result;
	}
	public static Result fail(ExceptionType exceptionType) {
		return new Result(exceptionType);
	}

	public static Result fail(String message) {
		// 注意：不能用 SuccessEnum.FAIL 的编码，它与 CommonConstants.SUCCESS 撞码(都是0)，
		// 会导致失败结果被 isSuccess() 误判为成功，这里必须用 CommonConstants.FAIL。
		return fail(CommonConstants.FAIL, message);
	}

	@SuppressWarnings("unchecked")
	public static <T> Result fail(ExceptionType exceptionType, T data) {
		return new Result(data, exceptionType.getMessage());
	}

	@SuppressWarnings("unchecked")
	public static Result fail(int code, String message) {
		return new Result(code, message, null);
	}

	@SuppressWarnings("unchecked")
	public static <T> Result fail(int code, String message, T data) {
		return new Result(code, message, data);
	}

	public Boolean isSuccess() {
		return this.getCode() == CommonConstants.SUCCESS || HttpStatus.OK.value() == this.getCode();
	}

}
