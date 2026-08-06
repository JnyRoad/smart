package com.tce.smart.platform.service.impl;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.ImageDTO;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.tool.enums.SmtImageEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 业务模块图片服务类
 *
 * @author mckaywu
 * @date 2019-05-30 11:46:36
 */
@Service
@Slf4j
public class ImageServiceImpl implements ImageService {
	@Value("${spring.image.base-url}")
	private String imageUrl;

	@Value("${spring.image.down-url}")
	private String downUrl;

	@Value("${spring.image.base-park-url}")
	private String parkImageUrl;

	@Autowired
	private SmtImageService smtImageService;

	@Autowired
	private ImageService imageService;

	@Resource
	private SmtDeviceService smtDeviceService;

	@Resource
	private RemoteDispatcherService remoteDispatcherService;

	@Override
	public String buildImageUrl(String imageId) {
		return smtImageService.buildImageUrl(imageUrl, imageId);
	}

	@Override
	public String buildImageUrl(Integer parkId, String imageId) {
		return smtImageService.buildImageUrl(parkImageUrl,parkId, imageId);
	}

	@Override
	public String buildDownloadUrl(String imageId,String fileName) {
		String realImageUrl = downUrl;

		if (com.tce.smart.common.core.util.StringUtils.isNotBlank(realImageUrl) && realImageUrl.contains("{image_id}") && com.tce.smart.common.core.util.StringUtils.isNotBlank(imageId)) {
			realImageUrl = realImageUrl.replace("{image_id}", imageId);
		}
		if (com.tce.smart.common.core.util.StringUtils.isNotBlank(realImageUrl) && realImageUrl.contains("{file_name}") && com.tce.smart.common.core.util.StringUtils.isNotBlank(fileName)) {
			realImageUrl = realImageUrl.replace("{file_name}", fileName);
		}

		return realImageUrl;
	}

	@Override
	public void saveDeviceUploadImg(String deviceId,String imgCode) {
		//根据设备编号查询设备信息
		SmtDevice smtDevice = smtDeviceService.getById(deviceId);
		if(null  == smtDevice){
			log.error("设备记录不存在,deviceCode:({})",deviceId);
			return;
		}
		//
		ImageDTO imageDTO = new ImageDTO();
		imageDTO.setParkId(smtDevice.getParkId());
		imageDTO.setId(imgCode);
		Result<String> imageRs = remoteDispatcherService.getImage(imageDTO, SecurityConstants.FROM_IN);
		Result<String> smallImageRs = remoteDispatcherService.getThumbnail(imageDTO, SecurityConstants.FROM_IN);
		if(StringUtils.isEmpty(imageRs.getData())){
			return;
		}
		smtImageService.saveImage(smtDevice.getParkId(),imgCode,imageRs.getData(),smallImageRs.getData(), SmtImageEnum.CAR_CAPTURE_IMG.getCode());
	}
}
