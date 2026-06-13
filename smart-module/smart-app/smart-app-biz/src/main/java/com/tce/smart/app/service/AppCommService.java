package com.tce.smart.app.service;

import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * description: App公共服务接口 <br>
 * date: 2019/11/13 11:00 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public interface AppCommService {
	/**
	 * 获取总部服务器图片访问URL
	 *
	 * @param imageId 图片id
	 * @return 图片访问URL 格式：http://....
	 */
	String buildHqImageUrl(String imageId);

	/**
	 * 获取文本内容图片访问URL
	 *
	 * @param contentTextId 文本内容ID
	 * @return 图片访问URL 格式：http://....
	 */
	String buildConentTextImageUrl(Integer contentTextId);

	/**
	 * 获取图片内容访问URL
	 *
	 * @param contentPicId 图片内容ID
	 * @return 图片访问URL 格式：http://....
	 */
	String buildContntPicImageUrl(Integer contentPicId);

	/**
	 * 获取模块图片访问URL
	 *
	 * @param moduleId 模块ID
	 * @return 图片访问URL 格式：http://....
	 */
	String buildModuleImageUrl(Integer moduleId);

	/**
	 * 获取广告图片访问URL
	 *
	 * @param adverId 模块ID
	 * @return 图片访问URL 格式：http://....
	 */
	String buildAdverImageUrl(Integer adverId);

	/**
	 * 获取文本内容图片二进制
	 *
	 * @param contentTextId 文本内容ID
	 * @return 图片二进制
	 */
	byte[] getContentTextImageByte(String contentTextId);

	/**
	 * 获取图片内容二进制
	 *
	 * @param contentPicId 图片内容ID
	 * @return 图片二进制
	 */
	byte[] getContentPicImageByte(String contentPicId);

	/**
	 * 获取模块图片二进制
	 *
	 * @param moduleId 模块ID
	 * @return 图片二进制
	 */
	byte[] getModuleImageByte(String moduleId);

	/**
	 * 获取广告图片二进制
	 *
	 * @param adverId 广告ID
	 * @return 图片二进制
	 */
	byte[] getAdverImageByte(String adverId);

	/**
	 * 获取总部服务器图片二进制
	 *
	 * @param imageId 图片id
	 * @return 图片二进制
	 */
	byte[] getHqImageByte(String imageId);

	/**
	 * 下载PDF文件
	 * @param subjectId
	 * @return 文件实体
	 * @throws IOException
	 */
	ResponseEntity<byte[]> downloadFdf(Integer subjectId) throws IOException;
}
