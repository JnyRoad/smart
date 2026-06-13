package com.tce.smart.platform.service.news;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.news.SaveNewsInfoReqDTO;
import com.tce.smart.platform.api.dto.req.news.SearchNewsListReqDTO;
import com.tce.smart.platform.core.entity.news.SmtNewsPublishDetails;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 18:00:02
 */
public interface SmtNewsPublishDetailsService extends IService<SmtNewsPublishDetails> {

	/**
	 * 分页查询
	 * @param page
	 * @param query
	 * @return
	 */
	IPage<SmtNewsPublishDetails> queryPage(Page page, SearchNewsListReqDTO query);

	/**
	 * 编辑信息
	 * @param saveNewsInfoReqDTO
	 * @return
	 */
	Boolean edit(SaveNewsInfoReqDTO saveNewsInfoReqDTO, SmtNewsTerminalService terminalService);

	/**
	 * 取消发布
	 * @param id
	 * @return
	 */
	Boolean cancelInfo(Long id);

	/**
	 * 发布
	 * @param id
	 * @return
	 */
	Boolean onlineInfo(Long id);

	/**
	 * 删除消息
	 * @param id
	 * @return
	 */
	Boolean deleteById(Long id,SmtNewsTerminalService smtNewsTerminalService);

}
