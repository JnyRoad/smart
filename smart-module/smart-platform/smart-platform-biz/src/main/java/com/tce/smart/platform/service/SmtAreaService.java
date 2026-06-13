package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtArea;
import com.tce.smart.platform.core.vo.AreaTreeRoot;
import com.tce.smart.platform.core.vo.SearchAreaVO;

import java.util.List;

/**
 * @author 齐佩
 * @date 2019-04-13 13:48:18
 */
public interface SmtAreaService extends IService<SmtArea> {

	Result addArea(SmtArea smtArea);

	Result updateAreaById(SmtArea smtArea);

	Result removeAreaById(Integer id);

	IPage<SearchAreaVO> getSmtAreaPage(Page page, SmtArea smtArea);

	List<AreaTreeRoot> getSmtAreaAll();

	/**
	 * 查询该园区的厂区
	 *
	 * @param parkId
	 * @param areName
	 * @return
	 */
	SmtArea getByName(Integer parkId, String areName);
}
