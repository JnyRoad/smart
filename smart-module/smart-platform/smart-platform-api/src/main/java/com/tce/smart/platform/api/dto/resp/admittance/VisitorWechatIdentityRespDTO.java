package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;

@Data
public class VisitorWechatIdentityRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 短时访客草稿会话，仅用于换取一次性动作 capability。 */
	private String visitorDraftToken;

	/** 与微信身份绑定的服务端草稿标识。 */
	private String visitorDraftId;
}
