package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterChangeQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterUpdateDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterChange;

/**
 * @author Li.JiaJun
 * @since 2022/5/12 10:53
 */
public interface SmtEleMeterChangeService extends IService<SmtEleMeterChange> {

	/**
	 * 分页查询
	 *
	 * @param page
	 * @param dto
	 * @return
	 */
	IPage<SmtEleMeterChange> getPage(Page page, EleMeterChangeQueryDTO dto);

	/**
	 * 新增电表更换记录
	 *
	 * @param beforeDto
	 * @param dto
	 */
	void addRecord(EleMeterUpdateDTO beforeDto, EleMeterUpdateDTO dto);
}
