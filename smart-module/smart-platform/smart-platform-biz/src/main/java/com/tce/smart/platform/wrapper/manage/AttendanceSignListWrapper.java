package com.tce.smart.platform.wrapper.manage;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.manage.AttendanceSignRespDTO;
import com.tce.smart.platform.core.vo.AttendanceSignVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: AttendanceSignListWrapper
 * @Author fushiping
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class AttendanceSignListWrapper extends BaseWrapper<AttendanceSignVO, AttendanceSignRespDTO> {

	@Override
	protected AttendanceSignRespDTO warp(AttendanceSignVO attendanceSignVO) throws IOException {
		AttendanceSignRespDTO attendanceSignRespDTO = BeanUtils.transform(AttendanceSignRespDTO.class, attendanceSignVO);
		//attendanceSignRespDTO.setIsObjectionDesc(ObjectionStatusEnum.desc(attendanceSignVO.getIsObjection()));
		return attendanceSignRespDTO;
	}
}
