package com.tce.smart.platform.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtParking;

/**
 * 停车场表
 *
 * @author 王艳勇
 * @date 2019-04-13 13:48:12
 */
public interface SmtParkingService extends IService<SmtParking> {

	boolean saveParking(SmtParking entity);

	boolean updateParking(SmtParking entity);

	boolean deleteParkingParking(Integer id);

	List<SmtParking> getParking(List<Integer> parkIds);

	IPage page(Page page, List<Integer> parkIds);

	/**
	 * 删除停车信息
	 * @param id
	 * @return
	 */
	boolean removeParking(String id);
}
