package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.MyDormitoryRespDTO;
import com.tce.smart.platform.core.vo.MyDormitoryVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Auther: guohongtai
 * @Date: 2020-09-07 14:57
 */
@Component
@AllArgsConstructor
public class MyDormitoryVOWrapper extends BaseWrapper<MyDormitoryVO, MyDormitoryRespDTO> {
	@Override
	protected MyDormitoryRespDTO warp(MyDormitoryVO myDormitoryVO) throws IOException {
		MyDormitoryRespDTO myDormitoryRespDTO = new MyDormitoryRespDTO();
		BeanUtil.copyProperties(myDormitoryVO, myDormitoryRespDTO);
		return myDormitoryRespDTO;
	}
}
