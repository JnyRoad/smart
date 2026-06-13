package com.tce.smart.platform.wrapper;

import cn.hutool.core.collection.CollUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.TempStaffEditRespDTO;
import com.tce.smart.platform.core.entity.SmtAppStaffAuth;
import com.tce.smart.platform.core.entity.SmtExternalDept;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.SmtAppStaffAuthService;
import com.tce.smart.platform.service.SmtExternalDeptService;
import com.tce.smart.tool.enums.StaffStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class TempStaffRespWrapper extends BaseWrapper<SmtStaff, TempStaffEditRespDTO> {
	@Autowired
	private SmtAppStaffAuthService smtAppStaffAuthService;
	@Autowired
	private ImageService imageService;
	@Autowired
	private SmtExternalDeptService smtExternalDeptService;
	@Override
	protected TempStaffEditRespDTO warp(SmtStaff staff) throws IOException {
		TempStaffEditRespDTO respDTO = BeanUtils.transform(TempStaffEditRespDTO.class, staff);
		List<SmtAppStaffAuth> auths = smtAppStaffAuthService.getStaffAuthList(staff.getId());
		if(CollUtil.isNotEmpty(auths)) {
			List<Integer> authList = auths.stream().map(SmtAppStaffAuth::getAuthId).collect(Collectors.toList());
			respDTO.setAppAuth(authList);
		}
		if(StringUtils.isNotEmpty(staff.getFacePicId())) {
			respDTO.setIsFace(Boolean.TRUE);
			//respDTO.setFaceImg(smtImageService.getImageBase64ByCode(staff.getFacePicId()));
			respDTO.setFaceImgUrl(imageService.buildImageUrl(staff.getFacePicId()));
		}
		if(staff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_TEMPORARY.getCode())) {
			respDTO.setStatus(StaffStatusEnum.STAFF_STATUS_IN.getCode());
		}
		SmtExternalDept smtExternalDept = smtExternalDeptService.getById(staff.getDepId());
		respDTO.setReportTo(smtExternalDept.getDirector());
		respDTO.setReportToName(smtExternalDept.getDirectorName());
		respDTO.setDepId(Long.parseLong(staff.getDepId()));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		respDTO.setEntryTime(df.format(staff.getCreateTime()));
		return respDTO;
	}
}
