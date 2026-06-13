package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.OvwYsjob;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public interface IOvwYsjobService extends IService<OvwYsjob> {

    List<OvwYsjob> getByDeptId(Integer deptId);

    OvwYsjob getByJobId(String jobId);

	Integer getByCompId(Integer compId);

	List<OvwYsjob> getListByCompId(Integer compId);

	/**
	 * 获取职位级层列表
	 * @return
	 */
	List<OvwYsjob> getJchenList();


}
