package com.tce.smart.platform.wrapper.news;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.news.NewsListRespDTO;
import com.tce.smart.platform.core.entity.news.SmtNewsPublishDetails;
import com.tce.smart.platform.core.entity.news.SmtNewsTerminal;
import com.tce.smart.platform.service.news.SmtNewsTerminalService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.NewsPublicStatusEnum;
import com.tce.smart.tool.enums.NewsPublicTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName:

 */
@Component
@AllArgsConstructor
public class NewsListWrapper extends BaseWrapper<SmtNewsPublishDetails, NewsListRespDTO> {

	private final SmtNewsTerminalService smtNewsTerminalService;

    @Override
    protected NewsListRespDTO warp(SmtNewsPublishDetails bean) throws IOException {
		NewsListRespDTO resp = BeanUtils.transform(NewsListRespDTO.class, bean);
		List<SmtNewsTerminal> terminal = smtNewsTerminalService.getByInfoId(bean.getId());
		resp.setStatus(NewsPublicStatusEnum.WAIT.getCode());
		if(CollUtil.isNotEmpty(terminal)) {
			List<String> terminalNames = terminal.stream().map(SmtNewsTerminal::getName).collect(Collectors.toList());
			resp.setTerminalName(StrUtil.join(SymbolConstants.COMMA, terminalNames));
			resp.setStatus(NewsPublicStatusEnum.RELEASE.getCode());
		}
		resp.setStatusDesc(NewsPublicStatusEnum.desc(resp.getStatus()));
		resp.setTypeDesc(NewsPublicTypeEnum.desc(bean.getType()));
        return resp;
    }
}
