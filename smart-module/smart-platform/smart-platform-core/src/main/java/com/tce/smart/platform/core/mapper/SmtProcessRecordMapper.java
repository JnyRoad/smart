package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.model.ProcessRecordFlow;

/**
 * 流程审批表
 *
 * @author 梁圆
 * @date 2019-05-15 11:34:54
 */
public interface SmtProcessRecordMapper extends BaseMapper<SmtProcessRecord> {

    List<SmtProcessRecord> getProcessRecord(@Param("processId") String processId);
    List<SmtProcessRecord> getHandoverRecord(@Param("processId") String processId,@Param("staffBadge") String staffBadge);
}
