package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtAlarmRecord;
import com.tce.smart.platform.core.vo.AlarmRecordVO;
import com.tce.smart.platform.service.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class AlarmRecordVOWrapper extends BaseWrapper<SmtAlarmRecord, AlarmRecordVO> {
	private final ImageService imageService;
    @Override
    protected AlarmRecordVO warp(SmtAlarmRecord alarmRecord) throws IOException {
	AlarmRecordVO alarmRecordVO = new AlarmRecordVO();
        BeanUtil.copyProperties(alarmRecord, alarmRecordVO);
//        Result<String> result = remoteBlobService.getBlob(alarmRecord.getSnapId(), SecurityConstants.FROM_IN);
//		alarmRecordVO.setSnapId(VehicleConstants.BASE64_PREFIX +  result.getData());
		if(StrUtil.isNotBlank(alarmRecord.getSnapId())){
			alarmRecordVO.setSnapId(imageService.buildImageUrl(alarmRecord.getSnapId()));
		}
        return alarmRecordVO;
    }
}
