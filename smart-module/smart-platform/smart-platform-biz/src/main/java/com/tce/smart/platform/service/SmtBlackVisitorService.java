package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.visitormanage.BlackVisitorAddReqDTO;
import com.tce.smart.platform.core.entity.SmtBlackVisitor;
import com.tce.smart.platform.core.vo.BlackVisitorVO;

import java.util.List;

public interface SmtBlackVisitorService extends IService<SmtBlackVisitor> {

	/**
	 * 移除黑名单访客
	 * @param id 黑名单访客id
	 * @return
	 */
	 Result<Boolean> removeVisitorById(Integer id);

	/**
	 * 添加黑名单访客
	 * @param smtBlackVisitor
	 * @return
	 */
	 Result<Boolean> addVisitor(BlackVisitorAddReqDTO smtBlackVisitor);

	/**
	 * 分页查询
	 * @param page
	 * @param smtBlackVisitor
	 * @return
	 */
	IPage<BlackVisitorVO> page(Page page, SmtBlackVisitor smtBlackVisitor);

	Result getHrBlackPage(Page page, String cerNo,String name);

	List<BlackVisitorAddReqDTO> batchImport(List<BlackVisitorAddReqDTO> reqDTO, Integer parkId);

}
