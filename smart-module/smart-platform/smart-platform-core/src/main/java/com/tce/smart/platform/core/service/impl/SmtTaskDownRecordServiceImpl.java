package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.dto.TaskDownRecordDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtTaskDownRecord;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceTaskMapper;
import com.tce.smart.platform.core.mapper.SmtTaskDownRecordMapper;
import com.tce.smart.platform.core.model.TaskDownRecordPark;
import com.tce.smart.platform.core.vo.TaskDownRecordVO;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务下发记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SmtTaskDownRecordServiceImpl extends ServiceImpl<SmtTaskDownRecordMapper, SmtTaskDownRecord> implements SmtTaskDownRecordService {

	private final SmtDeviceMapper smtDeviceMapper;

	private final StaffDeviceAuthSyncService staffDeviceAuthSyncService;
 private AuthOperationTransportGuard transportGuard;
 @org.springframework.beans.factory.annotation.Autowired
 public void setTransportGuard(AuthOperationTransportGuard guard){this.transportGuard=guard;}


	@Override
	public IPage<TaskDownRecordVO> getVehicle(Page page, TaskDownRecordDTO taskDownRecordDTO) {
		return this.baseMapper.getVehicle(page,taskDownRecordDTO);
	}

	@Override
	public IPage<TaskDownRecordVO> getPerson(Page page, TaskDownRecordDTO taskDownRecordDTO) {
		return this.baseMapper.getPerson(page,taskDownRecordDTO);
	}

	@Override
	public List<TaskDownRecordPark> getTree(List<Integer> parkIds, Integer type) {
		List<TaskDownRecordPark> list = this.baseMapper.getPark(parkIds);
		for (TaskDownRecordPark areaTree : list) {
			areaTree.setChildren(this.baseMapper.getDevice(areaTree.getValue(), type));
		}
		return list;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void handleTaskDownRecord(SmtDeviceTask smtDeviceTask) {
  com.tce.smart.platform.core.entity.SmtAuthTransportPhase current=AuthOperationTransportRecordContext.current("DIRECT",String.valueOf(smtDeviceTask.getId()));
  if(current!=null){handleVersionRecord(smtDeviceTask,current);return;}
  if(transportGuard!=null&&transportGuard.bound("DIRECT",String.valueOf(smtDeviceTask.getId())))throw new IllegalStateException("绑定任务必须经可信版本门禁维护记录");


		SmtTaskDownRecord taskDownRecord = this.getOne(new LambdaQueryWrapper<SmtTaskDownRecord>()
				.eq(SmtTaskDownRecord::getDeviceCode, smtDeviceTask.getDeviceCode())
				.eq(SmtTaskDownRecord::getCardNo, smtDeviceTask.getCardNo())
		);
		if(null == taskDownRecord
				&&
				(smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DOWN.getCode())
				|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DOWN.getCode())
						|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.UPDATE.getCode())
						|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_UPDATE.getCode()))){
			//下发成功操作
			addDownRecord(smtDeviceTask);
		} else if(null != taskDownRecord
				&&
				(smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DEL.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DEL.getCode())
			)){
			//删除
			removeRecordOrThrow(taskDownRecord);
			staffDeviceAuthSyncService.syncAfterDelete(taskDownRecord);
		} else if(null == taskDownRecord
				&&
				(smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DEL.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DEL.getCode())
			)){
			staffDeviceAuthSyncService.syncAfterDelete(smtDeviceTask.getDeviceCode(), smtDeviceTask.getCardNo(),
					smtDeviceTask.getGeneral(), smtDeviceTask.getDeviceType(), smtDeviceTask.getServiceType());
		} else if(null != taskDownRecord
				&&
				(smtDeviceTask.getAction().equals(DeviceTaskActionEnum.UPDATE.getCode())
						|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_UPDATE.getCode())
				)){
			//修改 先删除原来的记录 再添加新记录
			removeRecordOrThrow(taskDownRecord);
			addDownRecord(smtDeviceTask);
		}
	}

	/**
	 * 删除未成功时中断本地收敛，防止继续移除来源或写入替代记录。
	 */
	private void removeRecordOrThrow(SmtTaskDownRecord record) {
		if (!this.removeById(record.getId())) {
			throw new IllegalStateException("直连下发记录删除失败，recordId=" + record.getId());
		}
	}

	private void addDownRecord(SmtDeviceTask smtDeviceTask){
		SmtTaskDownRecord taskDownRecord = new SmtTaskDownRecord();
		BeanUtil.copyProperties(smtDeviceTask,taskDownRecord);
		taskDownRecord.setId(null);
		taskDownRecord.setImageId(smtDeviceTask.getImageId());
		taskDownRecord.setTaskId(smtDeviceTask.getId());
		taskDownRecord.setCreateTime(LocalDateTime.now());
		taskDownRecord.setStartTime(DateUtil.date(smtDeviceTask.getStartTime() * 1000));
		taskDownRecord.setOverTime(DateUtil.date(smtDeviceTask.getOverTime() * 1000));
		//下发记录表 只保存下发成功且未删除的记录
		taskDownRecord.setAction(DeviceTaskActionEnum.DOWN.getCode());
		SmtDevice smtDevice = smtDeviceMapper.selectById(smtDeviceTask.getDeviceCode());
		taskDownRecord.setParkId(smtDevice.getParkId());
		taskDownRecord.setServiceType(smtDeviceTask.getServiceType());
		taskDownRecord.setTaskType(DeviceTaskStatusEnum.SUCCESS.getCode());
		taskDownRecord.setRemark("");
		if (!this.save(taskDownRecord)) {
			throw new IllegalStateException("直连下发记录保存失败，taskId=" + smtDeviceTask.getId());
		}
	}

 /** 已通过目标版本门禁，只维护精确物理记录，业务来源由工作流最终收敛。 */
 private void handleVersionRecord(SmtDeviceTask task,com.tce.smart.platform.core.entity.SmtAuthTransportPhase phase) {
  LambdaQueryWrapper<SmtTaskDownRecord> query=new LambdaQueryWrapper<SmtTaskDownRecord>()
   .eq(SmtTaskDownRecord::getParkId,phase.getParkId()).eq(SmtTaskDownRecord::getDeviceCode,phase.getDeviceId())
   .eq(SmtTaskDownRecord::getCardNo,phase.getCardNo()).eq(SmtTaskDownRecord::getDeviceType,1)
   .eq(SmtTaskDownRecord::getServiceType,Integer.valueOf(phase.getServiceType()));
  SmtTaskDownRecord old=this.getOne(query);if(old!=null)removeRecordOrThrow(old);
  if("DELETE".equals(phase.getAction()))return;
  SmtTaskDownRecord record=new SmtTaskDownRecord();record.setParkId(phase.getParkId());record.setDeviceCode(phase.getDeviceId());
  record.setCardNo(phase.getCardNo());record.setDeviceType(1);record.setServiceType(Integer.valueOf(phase.getServiceType()));
  record.setTaskId(Integer.valueOf(phase.getTaskId()));record.setImageId(phase.getImageId());
  record.setStartTime(DateUtil.date(phase.getStartTime()*1000));record.setOverTime(DateUtil.date(phase.getOverTime()*1000));
  record.setAction(DeviceTaskActionEnum.DOWN.getCode());record.setTaskType(DeviceTaskStatusEnum.SUCCESS.getCode());
  record.setCreateTime(LocalDateTime.now());record.setRemark("");
  if(!this.save(record))throw new IllegalStateException("冻结下发记录保存失败");
 }
}
