package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.FaceImgTaskRespDTO;
import com.tce.smart.platform.core.entity.SmtFaceImgTask;
import com.tce.smart.platform.core.entity.SmtFaceImgTaskDetails;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.service.SmtFaceImgTaskDetailsService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: FaceImgTaskListWrapper
 * @Author fushiping
 */
@Component
@AllArgsConstructor
public class FaceImgTaskListWrapper extends BaseWrapper<SmtFaceImgTask, FaceImgTaskRespDTO> {

	private final SmtParkService smtParkService;

	private final SmtFaceImgTaskDetailsService smtFaceImgTaskDetailsService;

    @Override
    protected FaceImgTaskRespDTO warp(SmtFaceImgTask faceImgTask) throws IOException {
		FaceImgTaskRespDTO respDTO = new FaceImgTaskRespDTO();
        BeanUtil.copyProperties(faceImgTask, respDTO);
        //刷新任务同步状态
		Long taskId = faceImgTask.getId();
        smtFaceImgTaskDetailsService.syncTaskStatus(taskId);
		respDTO.setSuccessNum(smtFaceImgTaskDetailsService.countStatus(DeviceDownStatusEnum.SUCCESS.getCode(), taskId));
		SmtPark park = smtParkService.getById(faceImgTask.getParkId());
		if(Objects.nonNull(park)) {
			respDTO.setParkName(park.getParkName());
		}
		respDTO.setFailNum(smtFaceImgTaskDetailsService.countStatus(DeviceDownStatusEnum.FAIL.getCode(), taskId));
        return respDTO;
    }
}
