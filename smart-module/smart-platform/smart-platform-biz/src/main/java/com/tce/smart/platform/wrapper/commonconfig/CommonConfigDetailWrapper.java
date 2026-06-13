package com.tce.smart.platform.wrapper.commonconfig;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.CommonConfigRespDTO;
import com.tce.smart.platform.core.entity.SmtCommonConfig;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: CommonConfigDetailWrapper
 * @Author
 * @Date
 */
@Component
@AllArgsConstructor
public class CommonConfigDetailWrapper extends BaseWrapper<SmtCommonConfig, CommonConfigRespDTO> {

    @Override
    protected CommonConfigRespDTO warp(SmtCommonConfig bean) throws IOException {
		CommonConfigRespDTO resp = BeanUtils.transform(CommonConfigRespDTO.class, bean);
        return resp;
    }
}
