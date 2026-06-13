package com.tce.smart.platform.service.news;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.news.NewsTerminalReqDTO;
import com.tce.smart.platform.core.entity.news.SmtNewsTerminal;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 17:59:47
 */
public interface SmtNewsTerminalService extends IService<SmtNewsTerminal> {

	/**
	 * 根据信息id获得终端
	 * @param infoId
	 * @return
	 */
	List<SmtNewsTerminal> getByInfoId(Long infoId);

	/**
	 * 编辑终端信息
	 * @param reqDTO
	 * @return
	 */
	Boolean edit(NewsTerminalReqDTO reqDTO);

	/**
	 * 根据ip获得终端资源
	 * @param ip
	 */
	void getByTerminal(String ip);

	/**
	 * 检查资源是否到期
	 */
	void checkPublic();

}
