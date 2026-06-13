package com.tce.smart.businesstrip.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.businesstrip.core.entity.FormTableMain182;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 11:51
 */
public interface FormTableMain182Service extends IService<FormTableMain182> {

	/**
	 * 获取对象通过requestId
	 * @param requestId
	 * @return
	 */
	FormTableMain182 getByRequestId(String requestId);
}
