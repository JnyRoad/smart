package com.tce.smart.platform.service;

/***
 * description: 业务模块图片服务接口 <br>
 * date: 2019/12/11 9:23 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public interface ImageService {

	/**
	 * 获取图片访问URL
	 *
	 * @param imageId 图片id
	 * @return 图片访问URL 格式：http://....
	 */
	String buildImageUrl(String imageId);

	/**
	 * 获取图片访问地址
	 * @param parkId
	 * @param imageId
	 * @return
	 */
	String buildImageUrl(Integer parkId,String imageId);


	/**
	 * 获取下载URL
	 *
	 * @param imageId 图片id
	 * @return 格式：http://....
	 */
	String buildDownloadUrl(String imageId,String fileName);

	/**
	 * 保存设备抓取的上传图片
	 * @param deviceId  设备ID
	 * @param imgCodes  图片code
	 */
	void saveDeviceUploadImg(String deviceId,String imgCodes);
}
