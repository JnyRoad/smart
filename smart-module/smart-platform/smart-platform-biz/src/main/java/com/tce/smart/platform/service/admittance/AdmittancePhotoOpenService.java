package com.tce.smart.platform.service.admittance;

import java.util.List;

/**
 * 入厂申请照片开放接口 service。
 * 供开放 API（FileReceiver 等应用凭证方）拉取待同步的人脸照片：
 * 园区范围由调用方从应用 token 的 app_park_ids claim 推导后传入，本层不信任任何请求参数。
 */
public interface AdmittancePhotoOpenService {

	/**
	 * 查询指定园区范围内「审批通过、未过期、非车辆类型」申请单下全部随行人员的照片ID清单。
	 *
	 * @param allowedParkIds 应用绑定的园区范围（来自 token claim；空列表=拒绝一切数据，直接返回空）
	 * @return 去重后的 photoId 列表（不含空值；photoId 为随机 UUID，不含个人信息）
	 */
	List<String> listPendingPhotoIds(List<Integer> allowedParkIds);

	/**
	 * 按 photoId 读取照片二进制。
	 *
	 * @param photoId 照片ID（格式校验由控制器完成）
	 * @return 图片字节；不存在时返回 null（控制器映射为 404）
	 */
	byte[] loadPhoto(String photoId);
}
