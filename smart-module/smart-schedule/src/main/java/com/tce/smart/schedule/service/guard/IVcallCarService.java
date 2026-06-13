package com.tce.smart.schedule.service.guard;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 物流车预约
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
public interface IVcallCarService {

	/**
	 * 分页查询
	 * @param page
	 * @return
	 */
	IPage getVcallCarPage(Page page);
}
