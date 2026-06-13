package com.tce.smart.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.admin.api.entity.SysDept;

import java.util.List;

/**
 * <p>
 * 部门管理 Mapper 接口
 * </p>
 *
 */
public interface SysDeptMapper extends BaseMapper<SysDept> {

	/**
	 * 关联dept——relation
	 *
	 * @return 数据列表
	 */
	List<SysDept> listDepts();
}
