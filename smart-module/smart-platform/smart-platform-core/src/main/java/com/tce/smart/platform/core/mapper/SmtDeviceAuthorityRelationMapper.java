package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.model.DeviceTree;

/**
 * 设备权限表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:34
 */
public interface SmtDeviceAuthorityRelationMapper extends BaseMapper<SmtDeviceAuthorityRelation> {

    List<DeviceTree> getDevice(@Param("areaId") String areaId,@Param("types") List<Integer> type);

    List<DeviceTree> getPark(@Param("parkIds") List<Integer> parkIds);

    List<DeviceTree> getArea(@Param("parkId") Integer parkId, @Param("pId") Integer pId);

    List<String> getDeviceIds(@Param("authorityId") Integer authorityId,@Param("parkIds")  List<Integer> parkIds);

	List<SmtDeviceAuthorityRelation> getDevices(@Param("authorityId") Integer authorityId,@Param("parkIds")  List<Integer> parkIds);
}
