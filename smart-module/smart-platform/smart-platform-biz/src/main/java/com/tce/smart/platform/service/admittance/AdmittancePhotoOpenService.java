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
	 * 按 photoId 读取获授权园区内有效申请关联的照片二进制，仅查询，不写数据。
	 *
	 * @param photoId 照片ID（格式校验由控制器完成）
	 * @param allowedParkIds 应用 token 中的园区范围；null 或空列表直接拒绝
	 * @return 图片字节；无有效申请授权或图片不存在时返回 null（控制器统一映射为 404）
	 */
	byte[] loadPhoto(String photoId, List<Integer> allowedParkIds);
}
