package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.RouteCapability;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 只读持久接管范围与幂等核验，不创建、降级或释放接管事实。 */
public interface AuthOperationDirectTakeoverMapper {
    SmtDeviceTask task(@Param("id") Integer id);
    List<SmtAuthTransportPhase> taskPhases(@Param("task") String task);
    List<Integer> deviceParks(@Param("device") String device);
    int deviceHistory(@Param("device") String device);
    RouteCapability route(@Param("park") Integer park);
    int review(@Param("id") String id,@Param("park") Integer park,@Param("access") String access,
        @Param("device") String device,@Param("task") String task,@Param("reason") String reason);
    int reviewExists(@Param("id") String id);
}
