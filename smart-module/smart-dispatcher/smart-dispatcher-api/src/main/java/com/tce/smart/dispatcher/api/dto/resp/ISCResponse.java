package com.tce.smart.dispatcher.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sunfujian
 * @date 2021/8/23 14:52
 */
@Data
public class ISCResponse implements Serializable {
	private static final long serialVersionUID = -1L;

	private String code;

	private String msg;

	private Object data;
}
