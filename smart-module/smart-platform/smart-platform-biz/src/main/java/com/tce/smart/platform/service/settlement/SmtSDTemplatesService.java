package com.tce.smart.platform.service.settlement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsjobRespDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryFloor;
import com.tce.smart.platform.core.entity.SmtSdTemplates;

import java.util.List;

/**
 * @description: SmtSDTemplatesService
 * @date: 2020-07-01 14:45
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSDTemplatesService extends IService<SmtSdTemplates> {

	/**
	 * 根据条件分页查询水电模板
	 * @param page
	 * @param smtSdTemplates
	 * @return
	 */
	IPage getSmtDormitorySDTemplatePage(Page page, SmtSdTemplates smtSdTemplates);


	/**
	 * 根据园区ID查询所有水电模板
	 * @param parkid
	 * @return
	 */
	List<SmtSdTemplates> getSDTempByParkId(Integer parkid);

	/**
	 * 删除模板数据 并关联删除收费规则数据
	 * @param tempId
	 * @return
	 */
	boolean deleteSDTemplateData(Long tempId);

	/**
	 * 获取岗位级层列表
	 * @return
	 */
	List<OvwYsjobRespDTO> getJChenList();
}
