package com.tce.smart.algorithm.exception;

import com.tce.smart.algorithm.enums.AlgorithmExceptionEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AlgorithmException extends RuntimeException {

	/**
	 * 异常码
	 */
	private Integer code;

	public AlgorithmException(Integer code){
		this.code = code;
	}

	public AlgorithmException(Integer code, String message){
		super(message);
		this.code = code;
	}

	public AlgorithmException(AlgorithmExceptionEnum algorithmExceptionEnum){
		super(algorithmExceptionEnum.getMessage());
		this.code = algorithmExceptionEnum.getCode();
	}
}
