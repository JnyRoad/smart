package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author sunfujian
 * @date 2021/7/28 18:44
 */
public interface SmtDeviceTagMapper extends BaseMapper<SmtDeviceTag> {

	List<SmtDeviceTag> getByDeviceId(@Param("deviceId") String deviceId);
}
