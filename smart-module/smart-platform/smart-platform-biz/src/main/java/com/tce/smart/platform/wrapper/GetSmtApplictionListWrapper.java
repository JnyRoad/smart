package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.ApplicationListRespDTO;
import com.tce.smart.platform.core.vo.ApplicationListVO;
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
public class GetSmtApplictionListWrapper extends BaseWrapper<ApplicationListVO, ApplicationListRespDTO> {
    @Override
    protected ApplicationListRespDTO warp(ApplicationListVO applicationListVO) throws IOException {
		ApplicationListRespDTO applicationListRespDTO = new ApplicationListRespDTO();
        BeanUtil.copyProperties(applicationListVO, applicationListRespDTO);
        return applicationListRespDTO;
    }
}
