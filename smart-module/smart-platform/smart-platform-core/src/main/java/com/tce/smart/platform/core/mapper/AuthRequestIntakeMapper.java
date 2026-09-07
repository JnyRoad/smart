package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.core.entity.SmtAuthRequestIntake;
import com.tce.smart.platform.core.entity.SmtAuthOperationBatch;
import org.apache.ibatis.annotations.Param;
import java.util.List;
/** 请求header与整次操作子批次查询，不能在此预过滤调用者可访问的园区子集。 */
public interface AuthRequestIntakeMapper {
 SmtAuthRequestIntake find(@Param("actorId") Integer actorId,@Param("requestKey") String requestKey);
 int insert(SmtAuthRequestIntake row);
 int finish(SmtAuthRequestIntake row);
 List<SmtAuthOperationBatch> children(@Param("operationKey") String operationKey);
 int invalidSelections(@Param("operationKey") String operationKey);
}
