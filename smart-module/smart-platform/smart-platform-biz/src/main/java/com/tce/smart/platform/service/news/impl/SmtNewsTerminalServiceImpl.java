package com.tce.smart.platform.service.news.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.news.NewsTerminalReqDTO;
import com.tce.smart.platform.api.dto.resp.news.NewsDetailsRespDTO;
import com.tce.smart.platform.api.dto.resp.news.NewsFileRespDTO;
import com.tce.smart.platform.api.dto.resp.news.NewsImageRespDTO;
import com.tce.smart.platform.core.entity.news.SmtNewsInfoFile;
import com.tce.smart.platform.core.entity.news.SmtNewsInfoImage;
import com.tce.smart.platform.core.entity.news.SmtNewsPublishDetails;
import com.tce.smart.platform.core.entity.news.SmtNewsTerminal;
import com.tce.smart.platform.core.mapper.news.SmtNewsTerminalMapper;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.news.SmtNewsInfoFileService;
import com.tce.smart.platform.service.news.SmtNewsInfoImageService;
import com.tce.smart.platform.service.news.SmtNewsPublishDetailsService;
import com.tce.smart.platform.service.news.SmtNewsTerminalService;
import com.tce.smart.platform.websocket.WebSocketHandler;
import com.tce.smart.tool.enums.NewsPublicStatusEnum;
import com.tce.smart.tool.enums.NewsPublicTypeEnum;
import com.tce.smart.tool.enums.NewsTimeTypeEnum;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author fushiping
 * @date 2022-02-16 17:59:47
 */
@Service
@Slf4j
public class SmtNewsTerminalServiceImpl extends ServiceImpl<SmtNewsTerminalMapper, SmtNewsTerminal> implements SmtNewsTerminalService {

	@Autowired
	private SmtNewsPublishDetailsService detailsService;
	@Autowired
	private SmtNewsInfoImageService imageService;
	@Autowired
	private SmtNewsInfoFileService fileService;
	@Autowired
	private ImageService smtImageService;

	@Override
	public List<SmtNewsTerminal> getByInfoId(Long infoId) {
		return this.list(Wrappers.<SmtNewsTerminal>query().lambda().eq(SmtNewsTerminal::getInfoId, infoId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean edit(NewsTerminalReqDTO reqDTO) {
		SmtNewsTerminal terminal = BeanUtils.transform(SmtNewsTerminal.class, reqDTO);
		if (Objects.isNull(reqDTO.getId())) {
			terminal.setCreator(SecurityUtils.getUser().getUsername());
		}
		this.saveOrUpdate(terminal);
		Integer count = this.count(Wrappers.<SmtNewsTerminal>query()
				.lambda().eq(SmtNewsTerminal::getIp, reqDTO.getIp()));
		if (count > 1) {
			throw new SmartException("该IP已存在");
		}
		this.getByTerminal(reqDTO.getIp());
		return Boolean.TRUE;
	}

	@Override
	public void getByTerminal(String ip) {
		SmtNewsTerminal terminal = this.getByIP(ip);
		if (Objects.isNull(terminal)) {
			log.error("IP:{} 终端不存在", ip);
			return;
		}
		if (Objects.isNull(terminal.getInfoId())) {
			log.error("IP:{} 暂未绑定资源", ip);
			return;
		}
		if (NewsTimeTypeEnum.TIME_SLOT.getCode().equals(terminal.getTimeType())) {
			if (LocalDateTime.now().isBefore(terminal.getStartTime())) {
				if (Objects.isNull(terminal.getInfoId())) {
					log.error("IP:{} 资源尚未发布", ip);
					return;
				}
			}
		}

		SmtNewsPublishDetails details = detailsService.getById(terminal.getInfoId());
		if (Objects.isNull(details)) {
			log.error("IP:{} 所绑定资源不存在", ip);
			return;
		}
		NewsDetailsRespDTO detailsRespDTO = BeanUtils.transform(NewsDetailsRespDTO.class, details);
		if (NewsPublicTypeEnum.VIDEO.getCode().equals(details.getType())
				|| NewsPublicTypeEnum.PPT.getCode().equals(details.getType())) {
			SmtNewsInfoFile file = fileService.getById(Long.parseLong(details.getContent()));
			NewsFileRespDTO fileRespDTO = NewsFileRespDTO.builder().fileName(file.getFileName())
					.fileSuffix(file.getFileSuffix()).build();
			fileRespDTO.setFileUrl(fileService.buildFileUrl(file.getId().toString()));
			detailsRespDTO.setFile(fileRespDTO);
		}
		if (NewsPublicTypeEnum.IMAGE.getCode().equals(details.getType())) {
			List<SmtNewsInfoImage> images = imageService.getByInfoId(details.getId());
			if (CollUtil.isNotEmpty(images)) {
				List<NewsImageRespDTO> respDTO = new ArrayList<>();
				images.forEach(image -> {
					NewsImageRespDTO imageRespDTO = BeanUtils.transform(NewsImageRespDTO.class, image);
					imageRespDTO.setImageUrl(smtImageService.buildImageUrl(image.getImageId()));
					respDTO.add(imageRespDTO);
				});
				detailsRespDTO.setImages(respDTO);
			}
		}
		WebSocketHandler.sendMessageToAll(JSONUtil.toJsonStr(detailsRespDTO), ip);
	}

	public SmtNewsTerminal getByIP(String ip) {
		SmtNewsTerminal terminal = this.getOne(Wrappers.<SmtNewsTerminal>query()
				.lambda().eq(SmtNewsTerminal::getIp, ip));
		return terminal;
	}

	@Override
	public void checkPublic() {
		List<SmtNewsTerminal> terminals = this.list(Wrappers.<SmtNewsTerminal>query().lambda()
				.eq(SmtNewsTerminal::getTimeType, NewsTimeTypeEnum.TIME_SLOT.getCode())
				.ne(SmtNewsTerminal::getIsPublic, NewsPublicStatusEnum.RELEASE.getCode()));
		if (CollUtil.isEmpty(terminals)) {
			return;
		}
		terminals.forEach(item -> {
//			if (NewsPublicStatusEnum.RELEASE.getCode().equals(item.getIsPublic())) {
//				//检查是否过期
//				if (LocalDateTime.now().isAfter(item.getEndTime())) {
//					item.setIsPublic(NewsPublicStatusEnum.CANCEL.getCode());
//					this.getByTerminal(item.getIp());
//					this.updateById(item);
//				}
//			} else {
			//检查是否开始
			if (LocalDateTime.now().isAfter(item.getStartTime())) {
				item.setIsPublic(NewsPublicStatusEnum.RELEASE.getCode());
				this.getByTerminal(item.getIp());
				this.updateById(item);
			}
//			}
		});
	}


}
