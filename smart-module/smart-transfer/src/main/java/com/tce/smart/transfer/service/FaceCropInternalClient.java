package com.tce.smart.transfer.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.algorithm.api.dto.req.FaceImgCutReq;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import org.springframework.stereotype.Service;

/**
 * 历史图片迁移使用的内部人脸裁剪客户端。
 *
 * 该客户端始终声明服务令牌标记，由 Feign 拦截器申请最小 server scope 凭据；
 * 禁止迁移程序把员工人脸图片发送到外部匿名算法地址。
 */
@Service
public class FaceCropInternalClient {

	private final RemoteAlgorithmService remoteAlgorithmService;

	public FaceCropInternalClient(RemoteAlgorithmService remoteAlgorithmService) {
		this.remoteAlgorithmService = remoteAlgorithmService;
	}

	/**
	 * 通过内部算法契约裁剪 Base64 人脸图片。
	 *
	 * @param imageData 原始图片的 Base64 内容
	 * @return 裁剪后的 Base64 图片
	 */
	public String crop(String imageData) {
		if (StrUtil.isBlank(imageData)) {
			throw new TCEException("人脸图片不能为空");
		}
		FaceImgCutReq request = new FaceImgCutReq();
		request.setSerialNo(IdUtil.fastSimpleUUID());
		request.setImageData(imageData);
		Result<String> response = remoteAlgorithmService.cutFace(request, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		return response.data("内部人脸裁剪失败");
	}
}
