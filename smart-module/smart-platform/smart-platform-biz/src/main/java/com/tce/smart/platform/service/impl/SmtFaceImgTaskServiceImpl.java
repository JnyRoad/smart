package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.FaceImgTaskQueryReqDTO;
import com.tce.smart.platform.core.dto.CheckFacePicDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtFaceImgTaskMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.CheckFacePicVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import com.tce.smart.tool.enums.SmtImageEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * @author fushiping
 * @date 2021-07-20 17:44:40
 */
@Service
@Slf4j
public class SmtFaceImgTaskServiceImpl extends ServiceImpl<SmtFaceImgTaskMapper, SmtFaceImgTask> implements SmtFaceImgTaskService {

	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtFaceImgTaskDetailsService smtFaceImgTaskDetailsService;
	@Autowired
	private SmtImageService smtImageService;

	@Override
	public IPage<SmtFaceImgTask> getPage(Page page, FaceImgTaskQueryReqDTO query) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return this.page(page, Wrappers.<SmtFaceImgTask>query().lambda()
				.like(StringUtils.isNotEmpty(query.getTaskName()), SmtFaceImgTask::getTaskName, query.getTaskName())
				.eq(Objects.nonNull(query.getParkId()), SmtFaceImgTask::getParkId, query.getParkId())
				.in(CollUtil.isNotEmpty(parkIds), SmtFaceImgTask::getParkId, parkIds).orderByDesc(SmtFaceImgTask::getCreateTime));
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean checkFacePic(CheckFacePicDTO check) {
		log.info("更新人员照片:{}", JSONUtil.toJsonStr(check));
		Integer totalNum = check.getFacePicUpLoad().size();
		if (OneOrZeroEnum.ZERO.getCode() >= totalNum) {
			throw new TCEException("请选择上传图片");
		}
		//保存上传任务表
		Long taskId = this.saveTask(check.getParkId(), totalNum, check.getTaskName());
		for (CheckFacePicVO pic : check.getFacePicUpLoad()) {
			SmtStaff staff = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda()
					.eq(SmtStaff::getBadge, pic.getStaffBadge()));
			Integer status = DeviceDownStatusEnum.FAIL.getCode();
			String facePicId = null;
			String remark = null;
			Long staffId = null;
			if (ObjectUtil.isNotNull(staff)) {
				staffId = staff.getId();
				//保存图片
				facePicId = smtImageService.saveImage(OneOrZeroEnum.ZERO.getCode(),
						pic.getFacePic(), SmtImageEnum.TYPE_STAFF_FACE.getCode());
				staff.setFacePicId(facePicId);
				//更新人脸图片
				smtStaffService.updateById(staff);

				//上传人脸库
				Integer recordId = smtStaffService.createStaffPhotoUploadRecord(staff);
				try {
					String deviceRemark = smtStaffService.updatePersonCard(staff, pic.getFacePic(), facePicId, null, null, null);
					remark = appendRemark(remark, deviceRemark);
					//更新人脸并上传人脸到c6
					smtStaffService.faceStorage(staff, facePicId, recordId, facePicId);
					if (StringUtils.isEmpty(remark)) {
						status = DeviceDownStatusEnum.IN_WORK.getCode();
					}
				} catch (Exception e) {
					remark = appendRemark(remark, "设备下发任务异常");
				}
			} else {
				remark = "员工不存在";
			}
			SmtFaceImgTaskDetails details = SmtFaceImgTaskDetails.builder().createTime(LocalDateTime.now()).staffId(staffId)
					.imgCode(facePicId).imgName(pic.getStaffBadge()).remark(remark).status(status).taskId(taskId).build();
			smtFaceImgTaskDetailsService.save(details);
		}
		//修改下发任务表成功数量
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean deleteTask(Long taskId) {
		smtFaceImgTaskDetailsService.deleteTaskDetail(taskId);
		return this.removeById(taskId);
	}

	/**
	 * 新增下发任务表
	 *
	 * @param parkId
	 * @param totalNum
	 * @param taskName
	 * @return
	 */
	private Long saveTask(Integer parkId, Integer totalNum, String taskName) {
		SmtFaceImgTask task = SmtFaceImgTask.builder()
				.taskName(taskName)
				.parkId(parkId)
				.successNum(0)
				.createTime(LocalDateTime.now())
				.totalNum(totalNum).build();
		this.save(task);
		return task.getId();
	}

	/**
	 * 修改下发任务成功数
	 *
	 * @param taskId
	 * @param successNum
	 * @return
	 */
	private Boolean updateTask(Long taskId, Integer successNum) {
		return this.update(Wrappers.<SmtFaceImgTask>update().lambda()
				.set(SmtFaceImgTask::getSuccessNum, successNum)
				.eq(SmtFaceImgTask::getId, taskId));
	}

	private String appendRemark(String current, String append) {
		if (StringUtils.isEmpty(append)) {
			return current;
		}
		if (StringUtils.isEmpty(current)) {
			return append;
		}
		return current + "；" + append;
	}
}
