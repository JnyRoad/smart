package com.tce.smart.businesstrip.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.businesstrip.core.entity.VWorkflowSelectitem;
import com.tce.smart.businesstrip.core.mapper.VWorkflowSelectitemMapper;
import com.tce.smart.businesstrip.core.service.VWorkflowSelectitemService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class VWorkflowSelectitemServiceImpl extends ServiceImpl<VWorkflowSelectitemMapper, VWorkflowSelectitem>
		implements VWorkflowSelectitemService {

	/**
	 * 查询OA区域时，SELECTVALUE默认值
	 */
	private final Integer[] selectIds = new Integer[]{0, 5, 8, 11, 18, 14, 15, 20, 17};
	/**
	 * 查询OA区域时，FIELDID默认值
	 */
	private final Integer fieldId = 10254;

	@Override
	public List<VWorkflowSelectitem> getList(List<Integer> selectIdList, Integer fieldId) {
		List<VWorkflowSelectitem> list = this.baseMapper.getList(selectIdList, fieldId);
		return list;
	}
}
