package com.tce.smart.guard.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.guard.core.dto.QueryParkLogisticsDTO;
import com.tce.smart.guard.core.entity.VcallCar;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物流车预约
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
public interface GuardVcallCarService extends IService<VcallCar> {

	IPage getVcallCarPage(Page page, @Param("nowTime") String nowTime,
						  @Param("parkLogistics") List<QueryParkLogisticsDTO> parkLogistics);
}
