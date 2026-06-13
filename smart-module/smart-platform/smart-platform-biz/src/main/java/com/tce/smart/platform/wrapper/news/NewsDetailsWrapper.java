package com.tce.smart.platform.wrapper.news;

import cn.hutool.core.collection.CollUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.news.NewsDetailsRespDTO;
import com.tce.smart.platform.api.dto.resp.news.NewsFileRespDTO;
import com.tce.smart.platform.api.dto.resp.news.NewsImageRespDTO;
import com.tce.smart.platform.core.entity.news.SmtNewsInfoFile;
import com.tce.smart.platform.core.entity.news.SmtNewsInfoImage;
import com.tce.smart.platform.core.entity.news.SmtNewsPublishDetails;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.news.SmtNewsInfoFileService;
import com.tce.smart.platform.service.news.SmtNewsInfoImageService;
import com.tce.smart.tool.enums.NewsPublicTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName:
 */
@Component
@AllArgsConstructor
public class NewsDetailsWrapper extends BaseWrapper<SmtNewsPublishDetails, NewsDetailsRespDTO> {

	private final SmtNewsInfoFileService smtNewsInfoFileService;

	private final SmtNewsInfoImageService smtNewsInfoImageService;

	private final ImageService smtImageService;


	@Override
	protected NewsDetailsRespDTO warp(SmtNewsPublishDetails bean) throws IOException {
		NewsDetailsRespDTO resp = BeanUtils.transform(NewsDetailsRespDTO.class, bean);
		if (NewsPublicTypeEnum.VIDEO.getCode().equals(bean.getType())
				|| NewsPublicTypeEnum.PPT.getCode().equals(bean.getType())) {
			SmtNewsInfoFile file = smtNewsInfoFileService.getById(Long.parseLong(bean.getContent()));
			NewsFileRespDTO fileRespDTO = BeanUtils.transform(NewsFileRespDTO.class, file);
			if(NewsPublicTypeEnum.VIDEO.getCode().equals(bean.getType())) {
				fileRespDTO.setFileUrl(smtNewsInfoFileService.buildFileUrl(file.getId().toString()));
			}
			resp.setFile(fileRespDTO);
		}
		if (NewsPublicTypeEnum.IMAGE.getCode().equals(bean.getType())) {
			List<SmtNewsInfoImage> images = smtNewsInfoImageService.getByInfoId(bean.getId());
			if(CollUtil.isEmpty(images)) {
				return resp;
			}
			List<NewsImageRespDTO> respDTO = new ArrayList<>();
			images.forEach(image -> {
				NewsImageRespDTO imageRespDTO = BeanUtils.transform(NewsImageRespDTO.class, image);
				imageRespDTO.setImageUrl(smtImageService.buildImageUrl(image.getImageId()));
				respDTO.add(imageRespDTO);
			});
			resp.setImages(respDTO);
		}
		return resp;
	}
}
