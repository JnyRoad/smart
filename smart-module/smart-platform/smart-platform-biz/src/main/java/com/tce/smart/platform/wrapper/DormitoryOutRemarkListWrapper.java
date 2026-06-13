package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.DormitoryOutRemarkRespDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryOutRemark;
import com.tce.smart.tool.enums.DormitoryOutRemarkEnum;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-platform
 * @ClassName:
 * @Author fushiping
 * @Date 2019/5/2
 */
@Component
public class DormitoryOutRemarkListWrapper extends BaseWrapper<SmtDormitoryOutRemark, DormitoryOutRemarkRespDTO> {

	@Override
	protected DormitoryOutRemarkRespDTO warp(SmtDormitoryOutRemark model) throws IOException {
		DormitoryOutRemarkRespDTO dormitory = BeanUtils.toBean(model, DormitoryOutRemarkRespDTO.class);
		dormitory.setReasonTypeDesc(DormitoryOutRemarkEnum.desc(model.getReasonType()));
		return dormitory;
	}
}
