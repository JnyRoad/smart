package com.tce.smart.platform.service.news.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.news.SaveNewsInfoReqDTO;
import com.tce.smart.platform.api.dto.req.news.NewsInfoImageReqDTO;
import com.tce.smart.platform.api.dto.req.news.SearchNewsListReqDTO;
import com.tce.smart.platform.core.entity.news.SmtNewsInfoImage;
import com.tce.smart.platform.core.entity.news.SmtNewsPublishDetails;
import com.tce.smart.platform.core.entity.news.SmtNewsTerminal;
import com.tce.smart.platform.core.mapper.news.SmtNewsPublishDetailsMapper;
import com.tce.smart.platform.service.news.SmtNewsInfoFileService;
import com.tce.smart.platform.service.news.SmtNewsInfoImageService;
import com.tce.smart.platform.service.news.SmtNewsPublishDetailsService;
import com.tce.smart.platform.service.news.SmtNewsTerminalService;
import com.tce.smart.tool.enums.NewsPublicStatusEnum;
import com.tce.smart.tool.enums.NewsPublicTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2022-02-16 18:00:02
 */
@Service
public class SmtNewsPublishDetailsServiceImpl extends ServiceImpl<SmtNewsPublishDetailsMapper, SmtNewsPublishDetails> implements SmtNewsPublishDetailsService {

	@Autowired
	private SmtNewsInfoImageService smtNewsInfoImageService;
	@Autowired
	private SmtNewsInfoFileService smtNewsInfoFileService;

	@Override
	public IPage<SmtNewsPublishDetails> queryPage(Page page, SearchNewsListReqDTO query) {

		return this.page(page, Wrappers.<SmtNewsPublishDetails>query().lambda()
				.isNotNull(SmtNewsPublishDetails::getId)
				.eq(Objects.nonNull(query) && Objects.nonNull(query.getType()), SmtNewsPublishDetails::getType, query.getType())
				.like(Objects.nonNull(query) && StringUtils.isNotBlank(query.getInfoName()), SmtNewsPublishDetails::getInfoName, query.getInfoName())
				.between(Objects.nonNull(query) && Objects.nonNull(query.getStartTime()), SmtNewsPublishDetails::getCreateTime, query.getStartTime(), query.getEndTime())
				.last(Objects.nonNull(query) && NewsPublicStatusEnum.RELEASE.getCode().equals(query.getStatus()),
						"AND ID IN (SELECT INFO_ID FROM SMT_NEWS_TERMINAL WHERE INFO_ID IS NOT NULL) ORDER BY CREATE_TIME DESC")
				.last(Objects.nonNull(query) && NewsPublicStatusEnum.WAIT.getCode().equals(query.getStatus()),
						"AND ID NOT IN (SELECT INFO_ID FROM SMT_NEWS_TERMINAL WHERE INFO_ID IS NOT NULL) ORDER BY CREATE_TIME DESC"));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean edit(SaveNewsInfoReqDTO saveNewsInfoReqDTO, SmtNewsTerminalService terminalService) {
		SmtNewsPublishDetails details = BeanUtils.transform(SmtNewsPublishDetails.class, saveNewsInfoReqDTO);
		if (Objects.isNull(saveNewsInfoReqDTO.getId())) {
			details.setCreator(SecurityUtils.getUser().getUsername());
		}
		this.saveOrUpdate(details);
		if (NewsPublicTypeEnum.IMAGE.getCode().equals(details.getType())) {
			this.saveImg(saveNewsInfoReqDTO.getImageReqDTOS(), details.getId());
		}
		if (NewsPublicTypeEnum.PPT.getCode().equals(details.getType())
				& NewsPublicTypeEnum.VIDEO.getCode().equals(details.getType())) {
			//文件设为启用
			smtNewsInfoFileService.enableFile(details.getContent());
		}
		//更新大屏内容
		List<SmtNewsTerminal> terminal = terminalService.getByInfoId(details.getId());
		if (CollUtil.isNotEmpty(terminal)) {
			terminal.forEach(item -> {
				terminalService.getByTerminal(item.getIp());
			});
		}
		return Boolean.TRUE;
	}

	/**
	 * 保存轮播图片
	 *
	 * @param imageReqDTOS
	 * @param infoId
	 */
	private void saveImg(List<NewsInfoImageReqDTO> imageReqDTOS, Long infoId) {
		//删除原有图片
		smtNewsInfoImageService.removeByInfoId(infoId);
		if (CollUtil.isEmpty(imageReqDTOS)) {
			throw new SmartException("轮播图片为空");
		}
		List<SmtNewsInfoImage> images = imageReqDTOS.stream().map(req -> {
			SmtNewsInfoImage image = BeanUtils.transform(SmtNewsInfoImage.class, req);
			image.setInfoId(infoId);
			return image;
		}).collect(Collectors.toList());
		smtNewsInfoImageService.saveBatch(images);
	}

	@Override
	public Boolean cancelInfo(Long id) {
		return null;
	}

	@Override
	public Boolean onlineInfo(Long id) {
		SmtNewsPublishDetails details = SmtNewsPublishDetails.builder()
				.id(id).status(NewsPublicStatusEnum.RELEASE.getCode()).build();
		return details.updateById();
	}

	@Override
	public Boolean deleteById(Long id, SmtNewsTerminalService smtNewsTerminalService) {
		SmtNewsPublishDetails details = this.getById(id);
		//查询关联
		List<SmtNewsTerminal> list = smtNewsTerminalService.getByInfoId(id);
		if(CollUtil.isNotEmpty(list)) {
			throw new SmartException("该资源已关联终端，请先解除与终端的关联");
		}
		//删除图片
		if (NewsPublicTypeEnum.IMAGE.equals(details.getType())) {
			smtNewsInfoImageService.removeByInfoId(id);
		}
		//删除附件
		if (NewsPublicTypeEnum.VIDEO.equals(details.getType()) && NewsPublicTypeEnum.PPT.equals(details.getType())) {
			smtNewsInfoFileService.removeById(Long.parseLong(details.getContent()));
		}
		return this.removeById(id);
	}
}
