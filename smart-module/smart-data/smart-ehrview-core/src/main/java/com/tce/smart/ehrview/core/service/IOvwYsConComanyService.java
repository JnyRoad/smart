package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.OvwYsConComany;

import java.util.List;

/***
 * description: 合同签约单位服务接口 <br>
 * date: 2019/11/27 11:44 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public interface IOvwYsConComanyService extends IService<OvwYsConComany> {

	/**
	 * 根据compId查询
	 *
	 * @param compId compId
	 * @return OvwYsConComany
	 */
	OvwYsConComany getByCompId(Integer compId);

	/**
	 * 根据title查询列表
	 *
	 * @param title title
	 * @return List<OvwYsConComany>
	 */
	List<OvwYsConComany> getByTitle(String title);
}
