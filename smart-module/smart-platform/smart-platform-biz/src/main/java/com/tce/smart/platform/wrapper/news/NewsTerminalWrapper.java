package com.tce.smart.platform.wrapper.news;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.news.NewsListRespDTO;
import com.tce.smart.platform.api.dto.resp.news.NewsTerminalRespDTO;
import com.tce.smart.platform.core.entity.news.SmtNewsPublishDetails;
import com.tce.smart.platform.core.entity.news.SmtNewsTerminal;
import com.tce.smart.platform.service.news.SmtNewsPublishDetailsService;
import com.tce.smart.platform.service.news.SmtNewsTerminalService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.NewsPublicStatusEnum;
import com.tce.smart.tool.enums.NewsPublicTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName:

 */
@Component
@AllArgsConstructor
public class NewsTerminalWrapper extends BaseWrapper<SmtNewsTerminal, NewsTerminalRespDTO> {

	private final SmtNewsPublishDetailsService detailsService;

    @Override
    protected NewsTerminalRespDTO warp(SmtNewsTerminal bean) throws IOException {
		NewsTerminalRespDTO resp = BeanUtils.transform(NewsTerminalRespDTO.class, bean);
		SmtNewsPublishDetails details = detailsService.getById(bean.getInfoId());
		if(Objects.nonNull(details)) {
			resp.setInfoName(details.getInfoName());
			resp.setInfoType(NewsPublicTypeEnum.desc(details.getType()));
		}
        return resp;
    }
}
