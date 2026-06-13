package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtImage;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;

/***
 * description: 图片存储服务类 <br>
 * date: 2019/12/11 9:23 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public interface SmtImageService extends IService<SmtImage> {

	/**
	 * @param base64String 图片Base64字符串
	 * @param imageType 图片业务类型
	 * @return 图片编码
	 */
	String saveImage(Integer parkId, String base64String, Integer imageType);

	/**
	 * @param base64String 图片Base64字符串
	 * @param imgCode 图片标识
	*  @param smallImageBase64String 缩略图片Base64字符串
	 * @param imageType 图片业务类型
	 * @return 图片编码
	 */
	String saveImage(Integer parkId,String imgCode, String base64String,String smallImageBase64String, Integer imageType);

	/**
	 * 根据图片编码更新
	 *
	 * @param imageCode 图片编码
	 * @param base64String 原图Base64字符串
	 * @return 成功-true，失败-false
	 */
	Boolean updateByCode(String imageCode, String base64String);

	/**
	 * 根据图片编码更新
	 *
	 * @param imageCode 图片编码
	 * @param base64String 原图Base64字符串
	 * @param smallBase64String 缩略图Base64字符串
	 * @return 成功-true，失败-false
	 */
	Boolean updateByCode(String imageCode, String base64String, String smallBase64String);

	/**
	 * 根据图片编码删除
	 *
	 * @param imageCode 图片编码
	 * @return 成功-true，失败-false
	 */
	Boolean deleteByCode(String imageCode);

	/**
	 * 获取图片二进制
	 *
	 * @param imageCode 图片编号
	 * @return 图片
	 */
	SmtImage getByCode(String imageCode);

	/**
	 * 获取二进制原图
	 *
	 * @param imageCode 图片编号
	 * @return 图片二进制
	 */
	byte[] getImageBinaryByCode(String imageCode);

	/**
	 * 获取Base64字符串原图
	 *
	 * @param imageCode 图片编号
	 * @return 图片二进制
	 */
	String getImageBase64ByCode(String imageCode);

	/**
	 * 获取缩二进制略图
	 * @param imageCode 图片编号
	 * @return 图片二进制
	 */
	byte[] getSmallBinaryByCode(String imageCode);

	/**
	 * 获取Base64字符串缩略图
	 * @param imageCode 图片编号
	 * @return 图片Base64字符串
	 */
	String getSmallBase64ByCode(String imageCode);

	/**
	 * 获取图片访问URL
	 *
	 * @param baseImageUrl 图片访问baseurl
	 * @param imageId 图片id
	 * @return 图片访问URL 格式：http://....
	 */
	String buildImageUrl(String baseImageUrl,final String imageId);

	/**
	 * 获取图片访问URL
	 * @param baseImageUrl
	 * @param parkId
	 * @param imageId
	 * @return
	 */
	String buildImageUrl(String baseImageUrl,final Integer parkId,final String imageId);

	/**
	 * 下载
	 * @param code
	 * @return
	 */
	ResponseEntity<byte[]> download(String code,String name) throws UnsupportedEncodingException;
}
