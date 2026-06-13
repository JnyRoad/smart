package com.tce.smart.platform.wrapper.manage;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.req.manage.QueryEhrSetUpReqDTO;
import com.tce.smart.platform.api.dto.resp.manage.AttendanceSignRespDTO;
import com.tce.smart.platform.api.dto.resp.manage.EhrSetUpRespDTO;
import com.tce.smart.platform.core.entity.manage.SmtEhrSetUp;
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
public class EhrSerUpListtWrapper extends BaseWrapper<SmtEhrSetUp, EhrSetUpRespDTO> {

	@Override
	protected EhrSetUpRespDTO warp(SmtEhrSetUp smtEhrSetUp) throws IOException {
		EhrSetUpRespDTO attendanceSignRespDTO = BeanUtils.transform(EhrSetUpRespDTO.class, smtEhrSetUp);
		return attendanceSignRespDTO;
	}
}
