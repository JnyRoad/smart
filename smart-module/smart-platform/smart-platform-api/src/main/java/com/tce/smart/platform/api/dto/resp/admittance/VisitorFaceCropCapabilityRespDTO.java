package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;

/**
 * 单次访客人脸裁剪能力；只能放在请求头中使用，不能拼入 URL。
 */
@Data
public class VisitorFaceCropCapabilityRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String capability;
}
