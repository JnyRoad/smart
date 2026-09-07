package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.core.entity.SmtAuthSubjectCoord;
import org.apache.ibatis.annotations.Param;
/** 主体协调行只允许创建与加锁，不能删除或改变身份。 */
public interface SmtAuthSubjectCoordMapper {
    SmtAuthSubjectCoord lock(@Param("id") String id);
    int insert(SmtAuthSubjectCoord row);
}
