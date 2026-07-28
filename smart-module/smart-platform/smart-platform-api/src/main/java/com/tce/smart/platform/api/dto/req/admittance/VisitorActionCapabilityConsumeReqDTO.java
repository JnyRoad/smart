package com.tce.smart.platform.api.dto.req.admittance;

import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;
import lombok.Data;

import java.io.Serializable;

/** App 服务端通过内部 Feign 原子消费访客动作 capability 的请求。 */
@Data
public class VisitorActionCapabilityConsumeReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String capability;

	private String draftId;

	private VisitorActionCapabilityAction action;

	/** App 根据实际提交的图片计算的 SHA-256；图片动作必须与签发时绑定。 */
	private String payloadHash;
}
