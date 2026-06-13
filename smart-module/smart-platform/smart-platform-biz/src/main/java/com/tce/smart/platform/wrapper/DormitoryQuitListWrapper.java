package com.tce.smart.platform.wrapper;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.DormitoryQuitApplyRespDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtDormitoryStaffMapper;
import com.tce.smart.platform.core.service.SmtApprovalService;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.approval.ApprovalService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.ArticlesReleaseStatusEnum;
import com.tce.smart.tool.enums.DormitoryQuitReasonEnum;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @Description:
 * @ProjectName smart-module
 * @ClassName: DormitoryQuitListWrapper
 * @Author
 * @Date
 */
@Component
@AllArgsConstructor
public class DormitoryQuitListWrapper extends BaseWrapper<SmtDormitoryQuitApply, DormitoryQuitApplyRespDTO> {


	@Autowired
	private SmtDormitoryRoomService smtDormitoryRoomService;
	@Autowired
	private SmtDormitoryService smtDormitoryService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private ApproveListService approveListService;

    @Override
    protected DormitoryQuitApplyRespDTO warp(SmtDormitoryQuitApply bean) throws IOException {
		DormitoryQuitApplyRespDTO resp = BeanUtils.transform(DormitoryQuitApplyRespDTO.class, bean);
		resp.setQuitReasonDesc(DormitoryQuitReasonEnum.desc(bean.getQuitReason()));
		resp.setApplyLeaveTime(DateUtils.convert(bean.getApplyLeaveTime()));
		if(Objects.nonNull(bean.getIsHandle())) {
			resp.setIsHandle(OneOrZeroEnum.ZERO.getCode().equals(bean.getIsHandle()) ? "未处理" : "已处理");
		}
		//设置退宿楼栋
		int[] rooms = StringUtils.splitToInt(bean.getRoomIds(), SymbolConstants.COMMA);
		List<Integer> returnList = new ArrayList<>();
		returnList.addAll(IntStream.of(rooms).boxed().collect(Collectors.toList()));
		List<String> str = new ArrayList<>();
		Collection<SmtDormitoryRoom> staffDorList = smtDormitoryRoomService.listByIds(returnList);
		if(CollUtil.isNotEmpty(staffDorList)) {
			staffDorList.forEach(dor -> {
				StringBuilder sb = new StringBuilder();
				SmtPark park = smtParkService.getById(dor.getParkId());
				SmtDormitory dormitory = smtDormitoryService.getById(dor.getDormitoryId());
				sb.append(park.getParkName()).append(SymbolConstants.MINUS)
						.append(dormitory.getDormitoryName()).append(SymbolConstants.MINUS)
						.append(dor.getRoomName());
				str.add(sb.toString());
			});
		}
		resp.setDorDetailStr(str);
		//设置审批节点
		resp.setStatusDesc(ArticlesReleaseStatusEnum.desc(bean.getStatus()));
		if(ArticlesReleaseStatusEnum.PENDING_APPROVAL.getCode().equals(bean.getStatus())) {
			String nodeName = approveListService.getNewApprove(ApproveListStateEnum.PENDING.getCode(), bean.getId().toString());
			if(StringUtils.isNotEmpty(nodeName)) {
				resp.setStatusDesc(nodeName  +"(" +  ArticlesReleaseStatusEnum.PENDING_APPROVAL.getDesc() +")");
			}
		}
		if(ArticlesReleaseStatusEnum.APPROVAL_FAILED.getCode().equals(bean.getStatus())) {
			String nodeName = approveListService.getNewApprove(ApproveListStateEnum.REFUSE.getCode(), bean.getId().toString());
			if(StringUtils.isNotEmpty(nodeName)) {
				resp.setStatusDesc(nodeName +"(" + ArticlesReleaseStatusEnum.REFUSE.getDesc() +")");
			}
		}
		if(ArticlesReleaseStatusEnum.APPROVED.getCode().equals(bean.getStatus())) {
			resp.setStatusDesc("保安确认(待审批)");
		}
		if(ArticlesReleaseStatusEnum.DEPARTURE.getCode().equals(bean.getStatus())) {
			resp.setStatusDesc("审批通过");
		}
        return resp;
    }
}
