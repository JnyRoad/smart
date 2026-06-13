package com.tce.smart.platform.service;

public interface ISCPersonService {
	/**
	 * 更新ISC人员人脸
	 * @return
	 */
	Boolean updateISCPersonFace(String badge, Integer parkId, byte[] faceImg);

	Boolean updateISCPersonFace(String badge, Integer parkId, byte[] faceImg, String imageId);

	/**
	 * 同步人员实体卡号到指定园区的ISC平台。
	 */
	Boolean syncISCPersonCard(String badge, Integer parkId, String cardNo);

	/**
	 * 从指定园区的ISC平台删除人员实体卡号。
	 */
	Boolean deleteISCPersonCard(String badge, Integer parkId, String cardNo);

	/**
	 * 重试已落库的ISC人员/照片同步失败记录
	 */
	void retryFailedPersonFaceSync();
}
