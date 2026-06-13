package com.tce.smart.guard.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.guard.core.dto.QueryParkLogisticsDTO;
import com.tce.smart.guard.core.entity.VcallCar;
import com.tce.smart.guard.core.mapper.VcallCarMapper;
import com.tce.smart.guard.core.service.GuardVcallCarService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 物流车预约
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@Service
public class GuardVcallCarServiceImpl extends ServiceImpl<VcallCarMapper, VcallCar> implements GuardVcallCarService {

	@Override
	public IPage getVcallCarPage(Page page, String nowTime, List<QueryParkLogisticsDTO> parkLogistics) {
		return this.baseMapper.getVcallCarPage(page, nowTime, parkLogistics);
	}
}
