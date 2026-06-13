package com.tce.smart.platform.core.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtParking;

/**
 * 停车场管理表
 *
 * @author wangyanyong
 * @date 2019-04-13 13:48:12
 */
public interface SmtParkingMapper extends BaseMapper<SmtParking> {

	List<String> getParkingIds();

}
