package com.tce.smart.businesstrip.core.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.businesstrip.core.entity.VWorkflowSelectitem;

import java.util.List;


/**
 * oa区域服务
 */
public interface VWorkflowSelectitemService extends IService<VWorkflowSelectitem> {

	/**
	 * 获得区域列表
	 * @return
	 */
	List<VWorkflowSelectitem> getList(List<Integer> selectIdList, Integer fieldId);
}
