package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.dto.LeaveApplicationRecordDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.mapper.SmtLeaveApplicationMapper;
import com.tce.smart.platform.core.mapper.SmtProcessRecordMapper;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.core.vo.LeaveRecordVO;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 离职申请表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
@Service
@AllArgsConstructor
@Slf4j
public class SmtLeaveApplicationServiceImpl extends ServiceImpl<SmtLeaveApplicationMapper, SmtLeaveApplication> implements SmtLeaveApplicationService {

	private final SmtProcessRecordMapper processRecordMapper;

	@Override
	public IPage<LeaveRecordVO> getProcessRecord(Page page, String bagde, Integer leaveStatus) {
		return getProcessRecord(page, bagde, leaveStatus, null);
	}

	@Override
	public IPage<LeaveRecordVO> getProcessRecord(Page page, String bagde, Integer leaveStatus, Set<Integer> parkIds) {
//		List<SmtLeaveApplication> list = this.list(Wrappers.<SmtLeaveApplication>query().lambda().eq
//		(SmtLeaveApplication::getApplyBadge, bagde));
//		if(CollectionUtils.isNotEmpty(list)){
//			list.forEach(leaveApplication->getOAProcess(leaveApplication.getProcessId()));
//		}
		IPage<LeaveRecordVO> recordList = this.baseMapper.getLeaveRecordList(page, bagde, leaveStatus, parkIds);
		return recordList;
	}

	@Override
	public List<SmtProcessRecord> getLeaveApplication(String processId) {
//		if(StrUtil.isNotEmpty(processId)){
//			getOAProcess(processId);
//		}
		List<SmtProcessRecord> processRecord = processRecordMapper.getProcessRecord(processId);
		return processRecord;
	}

	@Override
	public IPage<SmtLeaveApplication> getPage(Page page, LeaveApplicationRecordDTO leaveApplicationRecordDTO) {
		return this.baseMapper.getPage(page,leaveApplicationRecordDTO);
	}

	@Override
	public SmtLeaveApplication getLeaveApplicationRecord(String processId) {
		SmtLeaveApplication leaveApplication = this.baseMapper.getLeaveApplicationByProcessId(processId);
		if (Objects.isNull(leaveApplication)) {
			throw new TCEException("离职申请不存在");
		}
		return leaveApplication;
	}

}
