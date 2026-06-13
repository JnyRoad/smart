package com.tce.smart.businesstrip.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.businesstrip.core.entity.FormTableMain182Dt2;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 11:51
 */
public interface FormTableMain182Dt2Service extends IService<FormTableMain182Dt2> {

	/**
	 * 获取list通过mainId
	 * @param mainId
	 * @return
	 */
	List<FormTableMain182Dt2> getByMainId(Integer mainId);

	/**
	 * 修改返厂时间
	 * @param dt2List
	 * @return
	 */
	Boolean updateFcsj(List<FormTableMain182Dt2> dt2List);
}
