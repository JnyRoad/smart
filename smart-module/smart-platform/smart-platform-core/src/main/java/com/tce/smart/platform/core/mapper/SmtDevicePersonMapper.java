package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.api.dto.req.DeviceAuthPersonReqDTO;
import com.tce.smart.platform.api.dto.resp.DeviceAuthPersonRespDTO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.DeviceDataDTO;
import com.tce.smart.platform.core.entity.SmtDevicePerson;

/**
 * 设备人员关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:38
 */
public interface SmtDevicePersonMapper extends BaseMapper<SmtDevicePerson> {

    IPage getDevicePerson(Page page,@Param("query") DeviceDataDTO devicePersonDTO);

    IPage getISCDevicePerson(Page page,@Param("query") DeviceDataDTO devicePersonDTO);

    IPage<DeviceAuthPersonRespDTO> getDeviceAuthPerson(Page page, @Param("query") DeviceAuthPersonReqDTO devicePersonDTO);

    IPage<DeviceAuthPersonRespDTO> getISCDeviceAuthPerson(Page page, @Param("query") DeviceAuthPersonReqDTO devicePersonDTO);
}
