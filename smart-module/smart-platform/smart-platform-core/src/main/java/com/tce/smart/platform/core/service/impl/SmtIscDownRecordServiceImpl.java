package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.dto.TaskDownRecordDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtIscDownRecordMapper;
import com.tce.smart.platform.core.model.TaskDownRecordPark;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.core.vo.TaskDownRecordVO;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务下发记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SmtIscDownRecordServiceImpl extends ServiceImpl<SmtIscDownRecordMapper, SmtIscDownRecord> implements SmtIscDownRecordService {

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

	/**
	 * 按已确认的 ISC 任务维护本地下发记录；记录写入失败必须抛出异常交由外层事务回滚。
	 *
	 * @param smtDeviceTask 已取得设备结果的 ISC 任务
	 */
	@Transactional
	@Override
	public void handleTaskDownRecord(SmtIscDeviceTask smtDeviceTask) {
  com.tce.smart.platform.core.entity.SmtAuthTransportPhase current=AuthOperationTransportRecordContext.current("ISC",String.valueOf(smtDeviceTask.getId()));
  if(current!=null){handleVersionRecord(smtDeviceTask,current);return;}
  if(transportGuard!=null&&transportGuard.bound("ISC",String.valueOf(smtDeviceTask.getId())))throw new IllegalStateException("绑定任务必须经可信版本门禁维护记录");

		if (isTemporaryAccessRecord(smtDeviceTask) && StrUtil.isBlank(smtDeviceTask.getPersonId())) {
			log.warn("临时人员ISC下发记录缺少personId，跳过记录维护，taskId={}, deviceCode={}, cardNo={}",
					smtDeviceTask.getId(), smtDeviceTask.getDeviceCode(), smtDeviceTask.getCardNo());
			return;
		}

		List<SmtIscDownRecord> taskDownRecords = this.list(buildDownRecordQuery(smtDeviceTask));
		if (taskDownRecords.isEmpty()) {
			if (smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DOWN.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DOWN.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.UPDATE.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_UPDATE.getCode())) {
				//下发成功操作
				addDownRecord(smtDeviceTask);
			} else if (smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DEL.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DEL.getCode())) {
				staffDeviceAuthSyncService.syncAfterDelete(smtDeviceTask.getDeviceCode(), smtDeviceTask.getCardNo(),
						smtDeviceTask.getGeneral(), smtDeviceTask.getDeviceType(), smtDeviceTask.getServiceType());
			}
		} else {
			if (smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DEL.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DEL.getCode())) {
				//删除
				removeDownRecords(taskDownRecords);
				staffDeviceAuthSyncService.syncAfterDelete(taskDownRecords.get(0));
			} else if (smtDeviceTask.getAction().equals(DeviceTaskActionEnum.UPDATE.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_UPDATE.getCode())) {
				//修改 先删除原来的记录 再添加新记录
				removeDownRecords(taskDownRecords);
				addDownRecord(smtDeviceTask);
			}
		}
	}

	/**
	 * 删除旧下发记录并显式检查批量删除结果，避免静默保留过期权限记录。
	 *
	 * @param taskDownRecords 待删除的旧记录
	 */
	private void removeDownRecords(List<SmtIscDownRecord> taskDownRecords) {
		boolean removed = this.removeByIds(taskDownRecords.stream().map(SmtIscDownRecord::getId).collect(Collectors.toList()));
		if (!removed) {
			throw new IllegalStateException("ISC下发记录删除失败，记录数量=" + taskDownRecords.size());
		}
	}

	LambdaQueryWrapper<SmtIscDownRecord> buildDownRecordQuery(SmtIscDeviceTask smtDeviceTask) {
		LambdaQueryWrapper<SmtIscDownRecord> query = new LambdaQueryWrapper<SmtIscDownRecord>()
				.eq(SmtIscDownRecord::getDeviceCode, smtDeviceTask.getDeviceCode())
				.eq(SmtIscDownRecord::getDeviceType, smtDeviceTask.getDeviceType());
		if (isStaffCardFaceServiceType(smtDeviceTask)) {
			query.in(SmtIscDownRecord::getServiceType,
					DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskConstants.UPDATE_FACE);
		} else {
			query.eq(SmtIscDownRecord::getServiceType, downRecordServiceType(smtDeviceTask.getServiceType()));
		}
		if (isTemporaryAccessRecord(smtDeviceTask) && StrUtil.isNotBlank(smtDeviceTask.getPersonId())) {
			query.eq(SmtIscDownRecord::getPersonId, smtDeviceTask.getPersonId());
			if (smtDeviceTask.getParkId() != null) {
				query.eq(SmtIscDownRecord::getParkId, smtDeviceTask.getParkId());
			}
			return query;
		}
		return query.eq(SmtIscDownRecord::getCardNo, smtDeviceTask.getCardNo());
	}

	private Integer downRecordServiceType(Integer serviceType) {
		if (DeviceTaskConstants.UPDATE_FACE.equals(serviceType)) {
			return DeviceTaskConstants.CARD_STAFF_IMPORT;
		}
		return serviceType;
	}

	private boolean isStaffCardFaceServiceType(SmtIscDeviceTask smtDeviceTask) {
		return smtDeviceTask != null
				&& DeviceTaskConstants.CARD.equals(smtDeviceTask.getDeviceType())
				&& (DeviceTaskConstants.CARD_STAFF_IMPORT.equals(smtDeviceTask.getServiceType())
				|| DeviceTaskConstants.UPDATE_FACE.equals(smtDeviceTask.getServiceType()));
	}

	private boolean isTemporaryAccessRecord(SmtIscDeviceTask smtDeviceTask) {
		return smtDeviceTask != null
				&& DeviceTaskConstants.CARD.equals(smtDeviceTask.getDeviceType())
				&& (DeviceTaskConstants.CARD_VISITOR.equals(smtDeviceTask.getServiceType())
				|| DeviceTaskConstants.CARD_ADMITTANCE.equals(smtDeviceTask.getServiceType()));
	}

	/**
	 * 新增成功下发记录并显式检查保存结果，避免任务成功但本地记录缺失。
	 *
	 * @param smtDeviceTask 已确认成功的 ISC 任务
	 */
	private void addDownRecord(SmtIscDeviceTask smtDeviceTask){
		SmtIscDownRecord taskDownRecord = new SmtIscDownRecord();
		BeanUtil.copyProperties(smtDeviceTask,taskDownRecord);
		taskDownRecord.setId(null);
		taskDownRecord.setPersonId(smtDeviceTask.getPersonId());
		taskDownRecord.setBadge(smtDeviceTask.getBadge());
		taskDownRecord.setImageId(smtDeviceTask.getImageId());
		taskDownRecord.setTaskId(smtDeviceTask.getId());
		taskDownRecord.setCreateTime(LocalDateTime.now());
		taskDownRecord.setStartTime(DateUtil.date(smtDeviceTask.getStartTime() * 1000));
		taskDownRecord.setOverTime(DateUtil.date(smtDeviceTask.getOverTime() * 1000));
		//下发记录表 只保存下发成功且未删除的记录
		taskDownRecord.setAction(DeviceTaskActionEnum.DOWN.getCode());
		SmtDevice smtDevice = smtDeviceMapper.selectById(smtDeviceTask.getDeviceCode());
		taskDownRecord.setParkId(smtDevice.getParkId());
		taskDownRecord.setServiceType(AuthOperationTransportRecordContext.current("ISC",String.valueOf(smtDeviceTask.getId()))==null
                ?downRecordServiceType(smtDeviceTask.getServiceType()):smtDeviceTask.getServiceType());
		taskDownRecord.setTaskType(DeviceTaskStatusEnum.SUCCESS.getCode());
		taskDownRecord.setRemark("");
		boolean saved = this.save(taskDownRecord);
		if (!saved) {
			throw new IllegalStateException("ISC下发记录保存失败，taskId=" + smtDeviceTask.getId());
		}
	}

 /** 已通过目标版本门禁，只维护精确物理记录，业务来源由工作流最终收敛。 */
 private void handleVersionRecord(SmtIscDeviceTask task,com.tce.smart.platform.core.entity.SmtAuthTransportPhase phase) {
  LambdaQueryWrapper<SmtIscDownRecord> query=new LambdaQueryWrapper<SmtIscDownRecord>()
   .eq(SmtIscDownRecord::getParkId,phase.getParkId()).eq(SmtIscDownRecord::getDeviceCode,phase.getDeviceId())
   .eq(SmtIscDownRecord::getCardNo,phase.getCardNo()).eq(SmtIscDownRecord::getDeviceType,1)
   .eq(SmtIscDownRecord::getServiceType,Integer.valueOf(phase.getServiceType()));
  List<SmtIscDownRecord> old=this.list(query);if(!old.isEmpty())removeDownRecords(old);
  if("DELETE".equals(phase.getAction()))return;
  SmtIscDownRecord record=new SmtIscDownRecord();record.setParkId(phase.getParkId());record.setDeviceCode(phase.getDeviceId());
  record.setCardNo(phase.getCardNo());record.setDeviceType(1);record.setServiceType(Integer.valueOf(phase.getServiceType()));
  record.setTaskId(Long.valueOf(phase.getTaskId()));record.setImageId(phase.getImageId());
  record.setStartTime(DateUtil.date(phase.getStartTime()*1000));record.setOverTime(DateUtil.date(phase.getOverTime()*1000));
  record.setAction(DeviceTaskActionEnum.DOWN.getCode());record.setTaskType(DeviceTaskStatusEnum.SUCCESS.getCode());
  record.setCreateTime(LocalDateTime.now());record.setRemark("");record.setPersonId(phase.getPersonId());record.setBadge(phase.getBadge());
  if(!this.save(record))throw new IllegalStateException("冻结下发记录保存失败");
 }
}
