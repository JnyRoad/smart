package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.VehicleAuthDTO;
import com.tce.smart.platform.core.dto.VehicleDTO;
import com.tce.smart.platform.core.dto.XcVehicleDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.platform.core.entity.SmtXcVehicle;
import com.tce.smart.platform.core.model.VehicleStaff;
import com.tce.smart.platform.core.vo.NotStaffVehicleVO;
import com.tce.smart.platform.core.vo.VehicleCountVO;
import com.tce.smart.platform.core.vo.VehicleVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 许昌车辆信息表
 *
 */
public interface SmtXcVehicleMapper extends BaseMapper<SmtXcVehicle> {
}
