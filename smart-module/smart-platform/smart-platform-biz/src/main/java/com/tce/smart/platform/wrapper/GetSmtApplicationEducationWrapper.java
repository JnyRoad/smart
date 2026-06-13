package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.EducationRespDTO;
import com.tce.smart.platform.core.vo.EducationVO;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

/***
 * description: GetSmtApplicationEducationWrapper.java <br>
 * date: 2019/12/24 11:32 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Component
@AllArgsConstructor
public class GetSmtApplicationEducationWrapper extends BaseWrapper<EducationVO, EducationRespDTO> {
    @Override
    protected EducationRespDTO warp(EducationVO educationVO) throws IOException {
		EducationRespDTO educationRespDTO = new EducationRespDTO();
		BeanUtils.copyProperties(educationVO,educationRespDTO);
        return educationRespDTO;
    }
}
