package com.tce.smart.app.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.tce.smart.app.controller.AppCommController;
import com.tce.smart.app.entity.AppAdverInfo;
import com.tce.smart.app.entity.AppContentPicture;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.service.*;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.feign.RemoteSmtImageService;
import com.tce.smart.tool.util.ImageUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * description: App公共服务实现类 <br>
 * date: 2019/11/13 11:01 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Service
public class AppCommServiceImpl implements AppCommService {

	@Value("${spring.file.image}")
	private String imageUrl;

	@Autowired
	private AppSubjectContentTextService appSubjectContentTextService;

	@Autowired
	private AppContentTextService appContentTextService;

	@Autowired
	private AppContentPictureService appContentPictureService;


	@Autowired
	private RemoteSmtImageService remoteSmtImageService;

	@Autowired
	private AppModuleInfoService moduleInfoService;

	@Autowired
	private AppAdverInfoService appAdverInfoService;

	@Override
	public String buildHqImageUrl(String imageId) {
		String realImageUrl = "";
		if (StringUtils.isNotBlank(imageUrl) && StringUtils.isNotEmpty(imageId)) {
			realImageUrl = imageUrl + AppCommController.NORMAL_IAMGE.replace(AppCommController.NORMAL_IAMGE_ID, imageId);
		}

		return realImageUrl;
	}

	@Override
	public String buildConentTextImageUrl(Integer contentTextId) {
		String realImageUrl = "";
		if (StringUtils.isNotBlank(imageUrl)) {
			realImageUrl = imageUrl + AppCommController.NEWS_IAMGE.replace(AppCommController.NORMAL_IAMGE_ID, String.valueOf(contentTextId));
		}

		return realImageUrl;
	}

	@Override
	public String buildContntPicImageUrl(Integer contentPicId) {
		String realImageUrl = "";
		if (StringUtils.isNotBlank(imageUrl)) {
			realImageUrl = imageUrl + AppCommController.CP_IAMGE.replace(AppCommController.NORMAL_IAMGE_ID, String.valueOf(contentPicId));
		}

		return realImageUrl;
	}

	@Override
	public String buildModuleImageUrl(Integer moduleId) {
		String realImageUrl = "";
		if (StringUtils.isNotBlank(imageUrl)) {
			realImageUrl = imageUrl + AppCommController.MODULE_IAMGE.replace(AppCommController.NORMAL_IAMGE_ID, String.valueOf(moduleId));
		}

		return realImageUrl;
	}

	@Override
	public String buildAdverImageUrl(Integer adverId) {
		String realImageUrl = "";
		if (StringUtils.isNotBlank(imageUrl)) {
			realImageUrl = imageUrl + AppCommController.ADVER_IAMGE.replace(AppCommController.NORMAL_IAMGE_ID, String.valueOf(adverId));
		}

		return realImageUrl;
	}

	@Override
	public byte[] getContentTextImageByte(String contentTextId) {
		byte[] rebyte = null;
		AppContentText contentText = appContentTextService.getById(contentTextId);
		if (Objects.nonNull(contentText.getPicBinary())) {
			String realBase64 = ImageUtils.removeHeader(new String(contentText.getPicBinary()));
			rebyte = ImageUtils.base64StrToByte(realBase64);
		}
		return rebyte;
	}

	@Override
	public byte[] getContentPicImageByte(String contentPicId) {
		byte[] rebyte = null;
		AppContentPicture contentPicture = appContentPictureService.getById(contentPicId);
		if (Objects.nonNull(contentPicture.getPicBinary())) {
			String realBase64 = ImageUtils.removeHeader(new String(contentPicture.getPicBinary()));
			rebyte = ImageUtils.base64StrToByte(realBase64);
		}
		return rebyte;
	}

	@Override
	public byte[] getModuleImageByte(String moduleId) {
		byte[] rebyte = null;
		AppModuleInfo appModuleInfo = moduleInfoService.getById(moduleId);
		if (Objects.nonNull(appModuleInfo.getModuleIcon())) {
			String realBase64 = ImageUtils.removeHeader(new String(appModuleInfo.getModuleIcon()));
			rebyte = ImageUtils.base64StrToByte(realBase64);
		}
		return rebyte;
	}

	@Override
	public byte[] getAdverImageByte(String adverId) {
		byte[] rebyte = null;
		AppAdverInfo appAdverInfo = appAdverInfoService.getById(adverId);
		if (ArrayUtil.isNotEmpty(appAdverInfo.getImageBinary())) {
			rebyte = appAdverInfo.getImageBinary();
		}
		return rebyte;
	}

	@Override
	public byte[] getHqImageByte(String imageId) {
		String imgBase64 = remoteSmtImageService.getImageBase64ByCode(imageId, SecurityConstants.FROM_IN).getData();
		byte[] bytes = ImageUtils.base64StrToByte(imgBase64);

		return bytes;
	}

	@Override
	public ResponseEntity<byte[]> downloadFdf(Integer subjectId) throws IOException {
		Integer textId = appSubjectContentTextService.getTextById(subjectId);
		AppContentText appContentText = appContentTextService.getById(textId);
		InputStream inputStream = null;
		inputStream = new ByteArrayInputStream(appContentText.getEnclosure());
		// 进行解码
		BASE64Decoder base64Decoder = new BASE64Decoder();
		byte[] enclosure = base64Decoder.decodeBuffer(inputStream);

		String enclosureName = new String(appContentText.getEnclosureName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentDispositionFormData("attachment", enclosureName);
		httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<>(enclosure, httpHeaders, HttpStatus.OK);
	}
}
