package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.businesstrip.CcdFormtableMainDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.VwHRMResourceRespDTO;
import com.tce.smart.data.api.feign.businesstrip.RemoteFormTableMainService;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.model.TravelDetail;
import com.tce.smart.tool.enums.IsBookingEnum;
import com.tce.smart.tool.enums.TripTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 * @author liangyuan
 *
 */
@Component
@AllArgsConstructor
public class TravelDetailWrapper extends BaseWrapper<CcdFormtableMainDTO, TravelDetail> {

	private final SmtStaffMapper smtStaffMapper;
	private final RemoteFormTableMainService remoteFormTableMainService;

	protected TravelDetail warp(CcdFormtableMainDTO ccdFormtableMain) throws IOException {
	TravelDetail travelDetail = new TravelDetail();
        BeanUtil.copyProperties(ccdFormtableMain, travelDetail);
        if(StringUtils.isNotEmpty(ccdFormtableMain.getPedestrianBadge())) {
	SmtStaff selectOne = smtStaffMapper.selectOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,ccdFormtableMain.getPedestrianBadge()));
	if(ObjectUtil.isNotNull(selectOne)) {
		travelDetail.setEmployeeId(selectOne.getId().toString());
		travelDetail.setEmployeeName(selectOne.getName());
		travelDetail.setDeptName(selectOne.getDepName());
		travelDetail.setJobName(selectOne.getJobName());
	}
        }
		 //判断出差人员确认名称
		 if(com.tce.smart.common.core.util.StringUtils.isNotBlank(travelDetail.getConfirmName())) {
			 StringBuffer confirmName = new StringBuffer();
			 String[] array = travelDetail.getConfirmName().split(",");//使用字符串逗号 ,切割字符串
				for (int i = 0; i < array.length; i++) {
					Result<VwHRMResourceRespDTO> result = remoteFormTableMainService.infoPerson(Integer.parseInt(array[i]), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
					if (CommonConstants.SUCCESS == result.getCode()) {
						VwHRMResourceRespDTO vwHRMResource = result.getData();
						confirmName.append(vwHRMResource.getLastName()).append(",");
					}
				}
				travelDetail.setConfirmName(confirmName.substring(0, confirmName.length()-1));
		 }
		 //判断出差代办人是否为空
		 if(ObjectUtil.isNotNull(travelDetail.getAgentName())) {
			 Result<VwHRMResourceRespDTO> result = remoteFormTableMainService.infoPerson(Integer.parseInt(travelDetail.getAgentName()), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				if (CommonConstants.SUCCESS == result.getCode()) {
					VwHRMResourceRespDTO vwHRMResource = result.getData();
					travelDetail.setAgentName(vwHRMResource.getLastName());
				}
		 }
        //判断出差类型是否为空
        if(ObjectUtil.isNotNull(travelDetail.getTripType())) {
	travelDetail.setTripTypeDesc(TripTypeEnum.desc(travelDetail.getTripType()));
        }
        //判断出差订飞机票是否为空
        if(ObjectUtil.isNotNull(travelDetail.getIsBooking())) {
	travelDetail.setIsBookingDesc(IsBookingEnum.desc(travelDetail.getIsBooking()));
        }
        return travelDetail;
    }
}
