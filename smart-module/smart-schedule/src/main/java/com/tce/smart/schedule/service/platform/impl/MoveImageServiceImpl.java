package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.ImageDTO;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.entity.SmtImage;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.schedule.service.platform.IMoveImageService;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 图片服务接口
 *
 * @author mckaywu
 * @date 2019-05-30 11:46:36
 */
@Service
@Slf4j
public class MoveImageServiceImpl implements IMoveImageService {

	@Autowired
	private SmtImageService smtImageService;

	//@Autowired
	//private RemoteBlobService remoteBlobService;
	@Autowired
	private RemoteDispatcherService remoteDispatcherService;


	@Override
	public void pageSaveHbaeImage() {
		QueryWrapper<SmtImage> queryWrapper = new QueryWrapper<>();
		queryWrapper
				.lambda()
				.isNotNull(SmtImage::getImageCode)
				.isNull(SmtImage::getImage);

		Page<SmtImage> page = new Page<>();
		restoreHbaseImage(page,queryWrapper);
		while (page.hasNext()) {
			page.setCurrent(page.getCurrent()+1);
			restoreHbaseImage(page,queryWrapper);
		}
	}

	/**
	 * 迁移Hbase图片
	 * @param page
	 * @param queryWrapper
	 */
	private void restoreHbaseImage(IPage<SmtImage> page, QueryWrapper<SmtImage> queryWrapper) {
		smtImageService.page(page, queryWrapper);
		log.info("restoreImage.page pgetTotal {}",page.getTotal());

		for (SmtImage element : page.getRecords()) {
			try {
				//Result<String> imageRs = remoteBlobService.getBlob(element.getImageCode(), SecurityConstants.FROM_IN);
				//Result<String> smallImageRs = remoteBlobService.getImageSmall(element.getImageCode(), SecurityConstants.FROM_IN);
				ImageDTO imageDTO = new ImageDTO();
				imageDTO.setParkId(element.getParkId());
				imageDTO.setId(element.getImageCode());
				Result<String> imageRs = remoteDispatcherService.getImage(imageDTO, SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				Result<String> smallImageRs = remoteDispatcherService.getThumbnail(imageDTO, SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				if (imageRs.isSuccess()) {
					//Hbase库图片不为空，则更新
					if(!StringUtil.isNullOrEmpty(imageRs.getData())) {
						smtImageService.updateByCode(element.getImageCode(), imageRs.getData(), smallImageRs.getData());
					}
					//Hbase库图片为空，删除待同步数据，免得一在同步
					else{
						smtImageService.deleteByCode(element.getImageCode());
					}
				}
			}catch(Exception e){
				log.error("图片转移出错",e);
			}
		}
	}


/*	*//**
	 * 处理缩略图
	 *
	 * @param imgBase64 图片base64字符串
	 * @param width     压缩宽度
	 * @param height    压缩高度
	 * @return 压缩后的图片二进制
	 *//*
	private byte[] image2Small(String imgBase64, int width, int height) {
		byte[] b_small = null;
		byte[] imageByte = ImageUtils.base64StrToByte(imgBase64);
		try {
			b_small = ImageUtils.fixedSizeByte(imageByte, width, height);
		} catch (Exception e) {
			log.warn("将图片转换为缩略图出错");
		}

		return b_small;
	}*/
}
