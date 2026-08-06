package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.dto.LeaveApplicationRecordDTO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.vo.LeaveRecordVO;

/**
 * 离职申请表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
public interface SmtLeaveApplicationMapper extends BaseMapper<SmtLeaveApplication> {

//    SmtLeaveApplication getLeaveApplication(@Param("badge") String badge);

    IPage<LeaveRecordVO> getLeaveRecordList(Page page,@Param("badge") String badge,@Param("leaveStatus") Integer leaveStatus);

    SmtLeaveApplication getLeaveApplicationByProcessId(@Param("processId") String processId);

    int updateStatus(@Param("approveStatus") Integer approveStatus,@Param("processId") String processId);

    String getPhoneByBadge(@Param("badge") String badge);

	IPage<SmtLeaveApplication> getPage(Page page, @Param("query") LeaveApplicationRecordDTO leaveApplicationRecordDTO);
}
