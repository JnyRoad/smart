package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.core.entity.SmtAuthDirectClaim;
import org.apache.ibatis.annotations.Param;
public interface AuthOperationDirectClaimMapper {
 com.tce.smart.platform.core.entity.SmtAuthTransportPhase phase(@Param("id") Long id);
 SmtAuthDirectClaim lock(@Param("id") String id);
 int insert(SmtAuthDirectClaim row);
}
