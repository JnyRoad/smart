package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.core.entity.*;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.sql.Timestamp;
/** 普通访客只读取真实业务行并持锁，不删除预约历史。 */
public interface VisitorAuthOperationMapper {
 SmtVisitor lockParent(@Param("park") Integer park,@Param("id") Long id);
 SmtFellowVisitor lockFellow(@Param("parent") Long parent,@Param("id") Long id);
 List<SmtFellowVisitor> lockFellows(@Param("parent") Long parent);
 SmtAuthSelectionSource frozenSource(@Param("batch") Long batch,@Param("ordinal") Long ordinal);
 int pendingParentCount(@Param("park") Integer park,@Param("parent") String parent);
 int vehicleExitEvidence(@Param("park") Integer park,@Param("parent") Long parent,@Param("event") Long event,@Param("from") Timestamp from,@Param("to") Timestamp to);
 int fellowExitEvidence(@Param("park") Integer park,@Param("fellow") Long fellow,@Param("event") Long event,@Param("from") Timestamp from,@Param("to") Timestamp to);
}
