package com.tce.smart.platform.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.resp.FaceImgTaskDetailsRespDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtFaceImgTaskDetailsMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtFaceImgTaskDetailsService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.ExportStatusEnum;
import com.tce.smart.tool.util.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-20 17:44:48
 */
@Service
public class SmtFaceImgTaskDetailsServiceImpl extends ServiceImpl<SmtFaceImgTaskDetailsMapper, SmtFaceImgTaskDetails> implements SmtFaceImgTaskDetailsService {

	@Autowired
	private SmtStaffServiceImpl smtStaffService;
	@Autowired
	private SmtStaffDeviceAuthService smtStaffDeviceAuthService;
	@Autowired
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;
	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean deleteTaskDetail(Long taskId) {
		List<SmtFaceImgTaskDetails> details = this.list(Wrappers.<SmtFaceImgTaskDetails>query()
				.lambda().eq(SmtFaceImgTaskDetails::getTaskId, taskId));
		if (CollUtil.isEmpty(details)) {
			return Boolean.FALSE;
		}
		for (SmtFaceImgTaskDetails detail : details) {
			if (detail.getStatus().equals(ExportStatusEnum.FAIL.getCode())) {
				continue;
			}
			SmtStaff smtStaff = smtStaffService.getById(detail.getStaffId());
			List<SmtStaffDeviceAuth> staffDeviceAuthList = smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>query()
					.lambda().eq(SmtStaffDeviceAuth::getStaffId, detail.getId()));
			if (CollUtil.isNotEmpty(staffDeviceAuthList)) {
				staffDeviceAuthList.forEach(auth -> {
					//设备权限表
					List<SmtDeviceAuthorityRelation> deviceAuthList = smtDeviceAuthorityRelationService
							.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
									.eq(SmtDeviceAuthorityRelation::getAuthorityId, auth.getAuthId()));
					//删除人员权限
					smtStaffService.savePersonCardTask(DeviceTaskConstants.DEL, DateUtil.currentSeconds(),
							DateUtil.currentSeconds(), smtStaff, deviceAuthList);
				});
			}
		}
		return this.remove(Wrappers.<SmtFaceImgTaskDetails>query().lambda()
				.eq(SmtFaceImgTaskDetails::getTaskId, taskId));
	}

	/**
	 * 下发任务是定时任务，建立任务后无法立刻得知设备下发状态
	 * 所以需要手动查询下发任务执行后，图片下发情况
	 * @param taskId
	 * @return
	 */
	@Override
	public Boolean syncTaskStatus(Long taskId) {
		//查询下发中任务
		List<SmtFaceImgTaskDetails> details = this.list(Wrappers.<SmtFaceImgTaskDetails>query()
				.lambda().eq(SmtFaceImgTaskDetails::getStatus, DeviceDownStatusEnum.IN_WORK.getCode())
				.eq( SmtFaceImgTaskDetails::getTaskId, taskId));
		if(CollUtil.isEmpty(details)){
			return Boolean.TRUE;
		}
		details.forEach(detail -> {
			detail.setStatus(DeviceDownStatusEnum.SUCCESS.getCode());
			//查询设备表中下发中任务的下发结果
			List<SmtDeviceTask> tasks = smtDeviceTaskService.list(Wrappers.<SmtDeviceTask>query().lambda()
					.eq(SmtDeviceTask::getCardNo, detail.getStaffId().toString()).eq(SmtDeviceTask::getImageId, detail.getImgCode()));
			for (SmtDeviceTask task : tasks) {
				if(DeviceTaskStatusEnum.FAIL.getCode().equals(task.getStatus())) {
					detail.setRemark(task.getRemark());
					detail.setStatus(DeviceDownStatusEnum.FAIL.getCode());
					continue;
				}
				if(DeviceTaskStatusEnum.INIT.getCode().equals(task.getStatus())) {
					detail.setStatus(DeviceDownStatusEnum.IN_WORK.getCode());
					detail.setRemark(task.getRemark());
					continue;
				}
			}
			this.updateById(detail);
		});
		return Boolean.TRUE;
	}

	@Override
	public List<SmtFaceImgTaskDetails> getByTaskId(Integer status,Long taskId) {
		//更新下发状态
		this.syncTaskStatus(taskId);
		List<SmtFaceImgTaskDetails> details = this.list(Wrappers.<SmtFaceImgTaskDetails>query()
				.lambda().eq(Objects.nonNull(status), SmtFaceImgTaskDetails::getStatus, status)
				.eq(Objects.nonNull(taskId), SmtFaceImgTaskDetails::getTaskId, taskId));
		return details;
	}

	@Override
	public IPage<SmtFaceImgTaskDetails> getPage(Page page, Integer status, Long taskId) {
		//更新下发状态
		this.syncTaskStatus(taskId);
		IPage<SmtFaceImgTaskDetails> details = this.page(page, Wrappers.<SmtFaceImgTaskDetails>query()
				.lambda().eq(Objects.nonNull(status), SmtFaceImgTaskDetails::getStatus, status)
				.eq(Objects.nonNull(taskId), SmtFaceImgTaskDetails::getTaskId, taskId));
		return details;
	}

	@Override
	public ResponseEntity<byte[]> downLoadExcel(Integer status, Long taskId) {
		List<SmtFaceImgTaskDetails> list = this.getByTaskId(status, taskId);
		if(CollectionUtils.isEmpty(list)) {
			throw new SmartException("暂无导入记录");
		}
		List<FaceImgTaskDetailsRespDTO> data = new ArrayList<>();
		list.forEach(details -> {
			FaceImgTaskDetailsRespDTO respDTO = BeanUtils.transform(FaceImgTaskDetailsRespDTO.class, details);
			respDTO.setStatusDesc(ExportStatusEnum.desc(details.getStatus()));
			respDTO.setCreateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(details.getCreateTime()));
			data.add(respDTO);
		});
		ResponseEntity<byte[]> responseEntity;
		String fileName = "照片批量导入详情.xls";
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), FaceImgTaskDetailsRespDTO.class, data)){
			responseEntity = IOUtils.getExcelResp(fileName, workbook);
		}catch (IOException e){
			log.error("excel导出异常", e);
			throw new SmartException(ExceptionEnum.UNKNOWN.getCode(), "excel导出异常");
		}
		return responseEntity;
	}

	@Override
	public Integer countStatus(Integer status, Long taskId) {
		return this.count(Wrappers.<SmtFaceImgTaskDetails>query().lambda()
				.eq(SmtFaceImgTaskDetails::getStatus, status)
				.eq(SmtFaceImgTaskDetails::getTaskId, taskId));
	}
}
