package com.tce.smart.platform.wrapper;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.DormitoryQuitApplyDetailRespDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.ArticlesReleaseStatusEnum;
import com.tce.smart.tool.enums.DormitoryQuitReasonEnum;
import com.tce.smart.tool.util.QRCodeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
@AllArgsConstructor
public class DormitoryQuitDetailsWrapper extends BaseWrapper<SmtDormitoryQuitApply, DormitoryQuitApplyDetailRespDTO> {

	@Autowired
	private SmtDormitoryRoomService smtDormitoryRoomService;
	@Autowired
	private ApproveListService approveListService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtDormitoryService smtDormitoryService;
	@Autowired
	private SmtParkService smtParkService;

	@Override
	protected DormitoryQuitApplyDetailRespDTO warp(SmtDormitoryQuitApply bean) throws IOException {
		DormitoryQuitApplyDetailRespDTO resp = BeanUtils.transform(DormitoryQuitApplyDetailRespDTO.class, bean);
		resp.setQuitReasonDesc(DormitoryQuitReasonEnum.desc(bean.getQuitReason()));
		//发起人人脸图片
		SmtStaff staff = smtStaffService.getById(bean.getStaffId());
		if (Objects.nonNull(staff.getFacePicId())) {
			resp.setFaceId(staff.getFacePicId());
		}
		//设置申请上传图片
		if (StrUtil.isNotBlank(bean.getImgs())) {
			List<String> imgList = new ArrayList<>();
			imgList.addAll(Stream.of(StringUtils.split(bean.getImgs(), SymbolConstants.COMMA)).collect(Collectors.toList()));
			resp.setImgs(imgList);
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
		//设置二维码
		if (StringUtils.isNotEmpty(bean.getSmsCode())) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", bean.getSmsCode());
				jsonObject.put("type", ApproveListTypeConstants.QUIT_DORMITORY.toString());
				resp.setQRcode(QRCodeUtils.wordsCreateQRCode(jsonObject.toString()));
			} catch (Exception e) {
				throw new SmartException("生成退宿申请二维码失败：" + e.getMessage());
			}
		}
		//设置审批节点
		resp.setStatusDesc(ArticlesReleaseStatusEnum.desc(bean.getStatus()));
		if (ArticlesReleaseStatusEnum.APPROVED.getCode().equals(bean.getStatus())) {
			resp.setStatusDesc("待保安确认");
		}
		if (ArticlesReleaseStatusEnum.DEPARTURE.getCode().equals(bean.getStatus())) {
			resp.setStatusDesc("已出厂");
		}
		//判断当前人员是否审批人
		resp.setIsApprove(Boolean.FALSE);
		String badge = SecurityUtils.getUser().getUsername();
		log.info("退宿审批工号:{}",badge);
		List<ApproveList> pendingApprove = approveListService.list(Wrappers.<ApproveList>query().lambda()
				.eq(ApproveList::getApproveBadge, badge)
				.eq(ApproveList::getBusinessId, bean.getId().toString())
				.eq(ApproveList::getApproveType, ApproveListTypeConstants.QUIT_DORMITORY)
				.eq(ApproveList::getApproveState, ApproveListStateEnum.PENDING.getCode()));
		if(CollUtil.isNotEmpty(pendingApprove)) {
			resp.setIsApprove(Boolean.TRUE);
		}
		log.info("退宿审批返回:{}",resp);
		//获得审批流程
		resp.setProcessRecord(approveListService.getProcess(bean.getId().toString(), bean.getName(), bean.getCreateTime()));

		return resp;
	}
}
