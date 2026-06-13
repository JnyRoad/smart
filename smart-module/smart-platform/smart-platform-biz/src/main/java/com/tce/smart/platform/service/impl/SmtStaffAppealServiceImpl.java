package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.AppealReplyReqDTO;
import com.tce.smart.platform.api.dto.req.SmtSecurityAreaSupplierReqDTO;
import com.tce.smart.platform.api.dto.req.SmtStaffAppealReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtStaffAppealListVO;
import com.tce.smart.platform.api.dto.resp.SmtStaffAppealQueryVO;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.dto.SmtSecurityAreaSupplierDTO;
import com.tce.smart.platform.core.dto.StaffAppealSearchDTO;
import com.tce.smart.platform.core.entity.ApproveList;
import com.tce.smart.platform.core.entity.SmtImage;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffAppeal;
import com.tce.smart.platform.core.mapper.SmtStaffAppealMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.SmtStaffAppealVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @description: SmtStaffAppealServiceImpl
 * @date: 2020-07-23 14:06
 * @author: wuling
 * @version: 1.0
 */
@Service
@AllArgsConstructor
public class SmtStaffAppealServiceImpl extends ServiceImpl<SmtStaffAppealMapper, SmtStaffAppeal> implements SmtStaffAppealService {

	private final SmtStaffAppealMapper smtStaffAppealMapper;

	private final SmtImageService smtImageService;

	private final ApproveListService approveListService;

	private final ImageService imageService;

	private final SmtStaffService smtStaffService;

	private final IAppMsgPushService appMsgPushService;

	@Override
	public IPage<SmtStaffAppealVO> getStaffAppealPage(Page page, StaffAppealSearchDTO staffAppealSearchDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		//按照需求 结束时间加一天
		if(null != staffAppealSearchDTO.getEndTime()){
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(staffAppealSearchDTO.getEndTime());
			calendar.add(Calendar.DATE,1);
			staffAppealSearchDTO.setEndTime(calendar.getTime());
		}
		IPage<SmtStaffAppealVO> staffAppealPage = smtStaffAppealMapper.getStaffAppealPage(page, staffAppealSearchDTO, parkIdList);
		for (SmtStaffAppealVO smtStaffAppealVO : staffAppealPage.getRecords()) {
			smtStaffAppealVO.setStatusDesc(AppealStatusEnum.desc(smtStaffAppealVO.getStatus()));
			smtStaffAppealVO.setAppealTypeDesc(AppealTypeEnum.desc(smtStaffAppealVO.getAppealType()));
		}
		return staffAppealPage;
	}

	@Override
	public IPage<SmtStaffAppealListVO> getStaffAppealRecord(Page page) {
		String staffBadge = SecurityUtils.getUser().getUsername();
		//这里不使用泛型的原因是 要把列表的结果替换成其他对象
		IPage staffAppealPage = smtStaffAppealMapper.getStaffAppealPage(page, StaffAppealSearchDTO.builder()
						.staffBadge(staffBadge)
						.build(),
				null);
		List<SmtStaffAppealListVO> listVOS = new ArrayList<>();
		for (Object obj : staffAppealPage.getRecords()) {
			SmtStaffAppealVO smtStaffAppealVO = (SmtStaffAppealVO) obj;
			smtStaffAppealVO.setStatusDesc(AppealStatusEnum.desc(smtStaffAppealVO.getStatus()));
			smtStaffAppealVO.setAppealTypeDesc(AppealTypeEnum.desc(smtStaffAppealVO.getAppealType()));
			SmtStaffAppealListVO smtStaffAppealListVO = new SmtStaffAppealListVO();
			BeanUtils.copyProperties(smtStaffAppealVO, smtStaffAppealListVO);
			//获取图片列表
			if (StringUtils.isNotEmpty(smtStaffAppealVO.getAppealImgs())) {
				List<String> imgUrlList = new ArrayList<>();
				List<String> strings = Arrays.asList(smtStaffAppealVO.getAppealImgs().split(","));
				strings.forEach(item -> {
					imgUrlList.add(imageService.buildImageUrl(item));
				});
				smtStaffAppealListVO.setAppealImgs(imgUrlList);
			}
			listVOS.add(smtStaffAppealListVO);
		}
		staffAppealPage.setRecords(listVOS);
		return staffAppealPage;
	}

	@Transactional
	@Override
	public boolean saveStaffAppealRecord(SmtStaffAppealReqDTO smtStaffAppealReqDTO) {
		String staffBadge = SecurityUtils.getUser().getUsername();
		SmtStaffAppeal smtStaffAppeal = new SmtStaffAppeal();
		StringBuilder imageCodes = new StringBuilder();
		BeanUtils.copyProperties(smtStaffAppealReqDTO, smtStaffAppeal);
		if (!CollectionUtil.isEmpty(smtStaffAppealReqDTO.getAppealImgList())) {
			//保存申诉图片
			for (String imgStr : smtStaffAppealReqDTO.getAppealImgList()) {
				String imageCode = smtImageService.saveImage(smtStaffAppealReqDTO.getParkId(), imgStr, SmtImageEnum.STAFF_APPEAL.getCode());
				if (!imageCodes.toString().equals("")) {
					imageCodes.append(",");
				}
				imageCodes.append(imageCode);
			}
		}
		smtStaffAppeal.setStaffBadge(staffBadge);
		smtStaffAppeal.setAppealImgs(imageCodes.toString());
		smtStaffAppeal.setCreateTime(new Date());
		smtStaffAppeal.setStatus(AppealStatusEnum.APPEAL.getCode());
		smtStaffAppeal.setIschange(AppealChangeEnum.NON_CHANGE.getCode());
		return this.save(smtStaffAppeal);
	}

	@Override
	public SmtStaffAppealQueryVO getAppealDetail(Long id) {

		SmtStaffAppealVO staffAppealDetail = smtStaffAppealMapper.getStaffAppealDetail(id);

		staffAppealDetail.setStatusDesc(AppealStatusEnum.desc(staffAppealDetail.getStatus()));
		staffAppealDetail.setAppealTypeDesc(AppealTypeEnum.desc(staffAppealDetail.getAppealType()));

		SmtStaffAppealQueryVO smtStaffAppealQueryVO = new SmtStaffAppealQueryVO();
		BeanUtils.copyProperties(staffAppealDetail, smtStaffAppealQueryVO);

		boolean isChange = AppealChangeEnum.CHANGED.getCode().equals(staffAppealDetail.getIschange());
		smtStaffAppealQueryVO.setIsChange(isChange);

		//如果存在图片列表
		if (StringUtils.isNotEmpty(staffAppealDetail.getAppealImgs())) {
			String[] imgCodes = staffAppealDetail.getAppealImgs().split(",");
			//图片访问地址链接
			List<String> imgLinks = new ArrayList<>();
			for (String code : imgCodes) {
				imgLinks.add(imageService.buildImageUrl(code));
			}
			smtStaffAppealQueryVO.setAppealImgs(imgLinks);
		}
		return smtStaffAppealQueryVO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveReplyDesc(AppealReplyReqDTO appealReplyReqDTO) {
		String username = SecurityUtils.getUser().getUsername();

		String changeBadge = changeBadge(appealReplyReqDTO.getId());
		if (StringUtils.isNotEmpty(changeBadge) && !username.equals(changeBadge)) {
			//已转交Ta人 并且当前用户和转交人工号不一致  则不能回复
			throw new TCEException("该用户不能回复");
		}

		Boolean result = approveListService.update(Wrappers.<ApproveList>lambdaUpdate()
				.eq(ApproveList::getBusinessId, appealReplyReqDTO.getId())
				.set(ApproveList::getApproveState, ApproveListStateEnum.AGREE.getCode()));

		if(result){//APP消息推送
			SmtStaffAppeal staffAppeal = this.smtStaffAppealMapper.selectById(appealReplyReqDTO.getId());
			AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
			appMsgPushDTO.setBadge(staffAppeal.getStaffBadge());
			appMsgPushDTO.setBussiessId(String.valueOf(appealReplyReqDTO.getId()));
			appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_APPEAL_10701.getCode());
			appMsgPushService.pushAppMsg(appMsgPushDTO);
		}

		//已申诉状态才能回复 不能重复回复
		return this.update(SmtStaffAppeal.builder()
						.replyName(username)
						.replyDesc(appealReplyReqDTO.getReplyDesc())
						.status(AppealStatusEnum.REPLY.getCode())
						.replyTime(new Date())
						.updateTime(new Date())
						.build(),
				new UpdateWrapper<SmtStaffAppeal>().lambda()
						.eq(SmtStaffAppeal::getId, appealReplyReqDTO.getId())
						.eq(SmtStaffAppeal::getStatus, AppealStatusEnum.APPEAL.getCode())
		);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean AddApproveList(Long id, String changeBadge) {
		boolean flag = smtStaffService.count(Wrappers.<SmtStaff>lambdaQuery()
				.eq(StrUtil.isNotBlank(changeBadge), SmtStaff::getBadge, changeBadge)) > 0;
		if (!flag) {
			throw new SmartException("请正确输入工号");
		}
		SmtStaffAppealVO staffAppealDetail = smtStaffAppealMapper.getStaffAppealDetail(id);

		if (AppealChangeEnum.CHANGED.getCode().equals(staffAppealDetail.getIschange())) {
			//已转交 不能重复转交
			throw new TCEException("不能重复转交");
		}

		//查看转交的员工是否存在
		int count = smtStaffService.count(new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getBadge, changeBadge));
		if (count <= 0) {
			//员工不存在
			throw new TCEException("转交员工不存在");
		}

		//修改转交状态
		this.updateById(SmtStaffAppeal.builder()
				.id(id)
				.ischange(AppealChangeEnum.CHANGED.getCode())
				.build());

		//添加待审批记录
		ApproveList approveList = new ApproveList();
		approveList.setBusinessId(id.toString());
		approveList.setApproveName(staffAppealDetail.getStaffName() + "提交的申诉");
		approveList.setApproveType(ApproveListTypeConstants.APPEAL);
		approveList.setApproveBadge(changeBadge);
		approveList.setApproveState(ApproveListStateEnum.PENDING.getCode());
		if(approveListService.saveApproveList(approveList)){
			AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
			appMsgPushDTO.setBadge(changeBadge);
			appMsgPushDTO.setApplicant(staffAppealDetail.getStaffName());
			appMsgPushDTO.setBussiessId(id.toString());
			appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_APPEAL_10702.getCode());
			appMsgPushService.pushAppMsg(appMsgPushDTO);
		}
		return true;
	}

	/**
	 * 查询转交人工号
	 *
	 * @param id
	 * @return
	 */
	private String changeBadge(Long id) {
		//查询是否已经转交Ta人
		List<ApproveList> approveLists = approveListService.list(new QueryWrapper<ApproveList>().lambda().eq(ApproveList::getBusinessId, id)
				.eq(ApproveList::getApproveType, ApproveListTypeConstants.APPEAL));
		if (CollectionUtil.isNotEmpty(approveLists)) {
			//已转交
			return approveLists.get(0).getApproveBadge();
		}
		return null;
	}

}
