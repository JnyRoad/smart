package com.tce.smart.guard.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.guard.core.dto.QueryParkLogisticsDTO;
import com.tce.smart.guard.core.entity.VcallCar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物流车预约
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@Mapper
public interface VcallCarMapper extends BaseMapper<VcallCar> {

	IPage getVcallCarPage(Page page, @Param("nowTime") String nowTime,
						  @Param("parkLogistics") List<QueryParkLogisticsDTO> parkLogistics);
}
