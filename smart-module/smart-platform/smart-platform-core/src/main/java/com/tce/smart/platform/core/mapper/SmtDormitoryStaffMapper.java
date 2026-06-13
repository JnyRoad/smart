package com.tce.smart.platform.core.mapper;

import java.util.List;

import com.tce.smart.platform.api.dto.req.remoteLock.LockDormitoryStaffDTO;
import com.tce.smart.platform.core.entity.ext.DormitoryRoomExt;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.InDormitoryDTO;
import com.tce.smart.platform.core.dto.StaffInDormitoryDTO;
import com.tce.smart.platform.core.entity.SmtCallowanceCancelRecord;
import com.tce.smart.platform.core.entity.SmtDormitoryBed;
import com.tce.smart.platform.core.entity.SmtDormitoryStaff;
import com.tce.smart.platform.core.entity.SmtOutDormitoryStaff;
import com.tce.smart.platform.core.vo.StaffInDormitoryVO;


/**
 * 员工宿舍信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
public interface SmtDormitoryStaffMapper extends BaseMapper<SmtDormitoryStaff> {

	IPage<StaffInDormitoryVO> getSmtDormitoryStaff(Page page, @Param("query") StaffInDormitoryDTO staffInDormitoryDTO,@Param("park") List<Integer> parkIdList);

	List<SmtDormitoryBed> getEmptyBed(InDormitoryDTO inDormitory);

	SmtDormitoryStaff getBedInfo(Integer id);

	List<SmtOutDormitoryStaff> getOutDormitoryStaff(@Param("staffBadge") String staffBadge, @Param("allowanceType") String allowanceType);

	List<SmtCallowanceCancelRecord> selectCallowanceCancelRecord(String staffBadge);

	List<DormitoryRoomExt> getRoomBedUse(@Param("rooms") List<Integer> rooms);

	List<LockDormitoryStaffDTO> getToLock(@Param("createTime") String createTime, @Param("parkId") Integer parkId);
}
