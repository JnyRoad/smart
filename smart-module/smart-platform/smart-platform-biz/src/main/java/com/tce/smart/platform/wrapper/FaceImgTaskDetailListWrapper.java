package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.FaceImgTaskDetailsRespDTO;
import com.tce.smart.platform.api.dto.resp.FaceImgTaskRespDTO;
import com.tce.smart.platform.core.entity.SmtFaceImgTask;
import com.tce.smart.platform.core.entity.SmtFaceImgTaskDetails;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.service.SmtFaceImgTaskService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import com.tce.smart.tool.enums.ExportStatusEnum;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: FaceImgTaskListWrapper
 * @Author fushiping
 */
@Component
@AllArgsConstructor
public class FaceImgTaskDetailListWrapper extends BaseWrapper<SmtFaceImgTaskDetails, FaceImgTaskDetailsRespDTO> {

    @Override
    protected FaceImgTaskDetailsRespDTO warp(SmtFaceImgTaskDetails faceImgTask) throws IOException {
		FaceImgTaskDetailsRespDTO respDTO = new FaceImgTaskDetailsRespDTO();
        BeanUtil.copyProperties(faceImgTask, respDTO);
        respDTO.setStatusDesc(DeviceDownStatusEnum.desc(faceImgTask.getStatus()));
		respDTO.setCreateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(faceImgTask.getCreateTime()));
        return respDTO;
    }
}
