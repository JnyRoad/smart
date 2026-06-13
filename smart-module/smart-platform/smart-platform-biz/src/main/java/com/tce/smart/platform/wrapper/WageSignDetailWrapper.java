package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtWageSign;
import com.tce.smart.platform.core.model.WageSignDetail;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.NoticeTypeEnum;
import com.tce.smart.tool.enums.WageSignStatusEnum;
import lombok.AllArgsConstructor;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
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
public class WageSignDetailWrapper extends BaseWrapper<SmtWageSign, WageSignDetail> {
    private final SmtStaffService staffService;

    private final SmtImageService smtImageService;

	@Autowired
	private SmtParkBuService smtParkBuService;

    @Override
    protected WageSignDetail warp(SmtWageSign wageSign) throws IOException {
	WageSignDetail wageSignDetail = new WageSignDetail();
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
        BeanUtil.copyProperties(wageSign, wageSignDetail);
        SmtStaff staff = staffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, wageSign.getBadge()));
        wageSignDetail.setCompName(staff.getCompName());
        wageSignDetail.setDepName(staff.getDepName());
        wageSignDetail.setName(staff.getName());
		wageSignDetail.setNoticeStatus(NoticeTypeEnum.desc(wageSign.getNoticeStatus()));
        if(ObjectUtil.isNotNull(wageSign.getSignImg())) {
			String imgBase64 = smtImageService.getImageBase64ByCode(wageSign.getSignImg());
			wageSignDetail.setSignImg(imgBase64);
		}
		List<SmtPark> parkList = smtParkBuService.getUserParkListByBu(Integer.parseInt(staff.getCompId()), parkIds);
		List<String> parkNames = parkList.stream().map(SmtPark::getParkName).collect(Collectors.toList());
		 wageSignDetail.setParkName(StringUtils.join(parkNames, SymbolConstants.COMMA));
		 wageSignDetail.setSignStatusDesc(WageSignStatusEnum.desc(wageSign.getSignStatus()));
		 if(Objects.nonNull(wageSign.getCreateTime())) {
			 wageSignDetail.setCreateTime(wageSign.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
		 }
        return wageSignDetail;
    }
}
