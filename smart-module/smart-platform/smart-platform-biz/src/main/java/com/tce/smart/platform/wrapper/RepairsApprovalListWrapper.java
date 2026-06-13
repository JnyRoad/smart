package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.RepairsApprovalListRespDTO;
import com.tce.smart.platform.core.dto.RepairsApprovalListDTO;
import com.tce.smart.platform.core.entity.SmtAlarmRecord;
import com.tce.smart.platform.core.entity.SmtApprovalNode;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.platform.core.vo.AlarmRecordVO;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.tool.enums.ApprovalPersonRuleEnum;
import com.tce.smart.tool.enums.DormitoryRepairStatusEnum;
import com.tce.smart.tool.enums.RangeTypeEnum;
import com.tce.smart.tool.enums.RepairSTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author
 * @Date
 */
@Component
@AllArgsConstructor
public class RepairsApprovalListWrapper extends BaseWrapper<RepairsApprovalListDTO, RepairsApprovalListRespDTO> {

	private final SmtApprovalNodeService smtApprovalNodeService;

    @Override
    protected RepairsApprovalListRespDTO warp(RepairsApprovalListDTO bean) throws IOException {
		RepairsApprovalListRespDTO resp = BeanUtils.transform(RepairsApprovalListRespDTO.class, bean);
		resp.setApproveId(bean.getBusinessId());
		resp.setRangeTypeDesc(RangeTypeEnum.desc(bean.getRangeType()));
		resp.setRepairTypeDesc(RepairSTypeEnum.desc(bean.getRepairType()));
		SmtApprovalNode node = smtApprovalNodeService.getById(bean.getNodeId());
		if(Objects.nonNull(node)) {
			if(ApprovalPersonRuleEnum.EXIST.getCode().equals(node.getIsExistApprover())) {
				resp.setStatusDesc("宿管(" + DormitoryRepairStatusEnum.desc(bean.getStatus()) + ")");
				return resp;
			}
			resp.setStatusDesc(ApprovalPersonRuleEnum.desc(node.getIsExistApprover()) + "("
					+ DormitoryRepairStatusEnum.desc(bean.getStatus()) + ")");
		}
        return resp;
    }
}
