package com.tce.smart.platform.core.mapper;

import java.util.List;

import com.tce.smart.platform.core.entity.SmtStaff;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.dto.LeaveHandoverItemDTO;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;

/**
 * 工作交接
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
public interface SmtLeaveHandoverMapper extends BaseMapper<SmtLeaveHandover> {

	/**
	 * 初始化交接信息
	 * @param list
	 * @return
	 */
   int initLeaveHandover(List<SmtLeaveHandover> list);

   /**
    * 开始交接
    * @param entitty
    * @return
    */
   int startLeaveHandover(@Param("query") SmtLeaveHandover entitty);

   /**
    * 交接确认
    * @param list
    * @param jjr
    * @param closed
    * @param closedTime
    * @param applicationId
    * @return
    */
   int endLeaveHandover(@Param("list") List<LeaveHandoverItemDTO> list,@Param("jjr") String jjr,@Param("closed") Integer closed,@Param("closedTime") String closedTime,@Param("applicationId") Integer applicationId);

   /**
    * 回写
    * @param applicationId
    * @return
    */
   List<SmtLeaveHandover> writebackLeavehandover(@Param("applicationId")  Integer applicationId);

   /**
    * 根据员工号获取员工Id
    * @param badge
    * @return
    */
   SmtStaff getStaffByBadge(@Param("badge") String badge);

   /**
    * 删除离职员工宿舍
    * @param staffId
    * @return
    */
   int deleteRoomByStaffId(@Param("staffId") Long staffId);

   /**
    * 更新员工状态为离职状态
    * @param status
    * @param staffId
    * @return
    */
   int updateStatusByStaffId(@Param("status") Integer status,@Param("staffId") Long staffId);

   /**
    * 查询交接未完成数量
    * @param applicationId
    * @param jjClose
    * @return
    */
   int getHandoverCount(@Param("applicationId") Integer applicationId,@Param("jjClose") Integer jjClose);

   /**
    * 获取交接部门
    * @param applicationId
    * @param jjr
    * @param jjBegin
    * @return
    */
   List<SmtLeaveHandover> getLeaveHandover(@Param("applicationId") Integer applicationId,@Param("jjr") String jjr,@Param("jjBegin") Integer jjBegin);

   /**
    * 获取交接项
    * @param applicationId
    * @param jjr
    * @param jjBegin
    * @param jjClosed
    * @return
    */
   List<SmtLeaveHandover> getLeaveHandoverItem(@Param("applicationId") Integer applicationId,@Param("jjr") String jjr,@Param("jjBegin") Integer jjBegin,@Param("jjClosed") Integer jjClosed,@Param("zrdep") Integer zrdep);

   /**
    * 安装不同员工和分钟插入待审批列表里
    * @param applicationId
    */
   List<SmtLeaveHandover> getApproveList(Integer applicationId);
}
