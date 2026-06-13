package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.Led;
import com.tce.smart.platform.api.dto.resp.LedAreaRespDTO;
import com.tce.smart.platform.api.dto.resp.LedLineRespDTO;
import com.tce.smart.tool.constant.LedConstants;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
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
public class LetLineWrapper extends BaseWrapper<Led, LedLineRespDTO> {

    @Override
    protected LedLineRespDTO warp(Led led) throws IOException {
		LedLineRespDTO ledLineVO = new LedLineRespDTO();
		LedAreaRespDTO line1 = new LedAreaRespDTO(LedConstants.LINE_1, 0, "", 0, 0);
		LedAreaRespDTO line2 = new LedAreaRespDTO(LedConstants.LINE_2, 0, "", 0, 0);
		LedAreaRespDTO line3 = new LedAreaRespDTO(LedConstants.LINE_3, 0, "", 0, 0);
		LedAreaRespDTO line4 = new LedAreaRespDTO(LedConstants.LINE_4, 0, "", 0, 0);
		ledLineVO.setLine1(line1);
		ledLineVO.setLine2(line2);
		ledLineVO.setLine3(line3);
		ledLineVO.setLine4(line4);
        BeanUtil.copyProperties(led, ledLineVO);
		if(ObjectUtil.isNotNull(led)){
			BeanUtil.copyProperties(led, ledLineVO);
			if(CollUtil.isNotEmpty(led.getLedAreaList())){
				led.getLedAreaList().forEach(ledArea -> {
					if(ObjectUtil.isNotNull(ledArea.getAreaRow())){
						if(ledArea.getAreaRow().equals(LedConstants.LINE_1)){
							BeanUtils.copyProperties(ledArea,ledLineVO.getLine1());
						}else if(ledArea.getAreaRow().equals(LedConstants.LINE_2)){
							BeanUtils.copyProperties(ledArea,ledLineVO.getLine2());
						}else if(ledArea.getAreaRow().equals(LedConstants.LINE_3)){
							BeanUtils.copyProperties(ledArea,ledLineVO.getLine3());
						}else if(ledArea.getAreaRow().equals(LedConstants.LINE_4)){
							BeanUtils.copyProperties(ledArea,ledLineVO.getLine4());
						}
					}
				});
			}
		}
		ledLineVO.setSoundText(StrUtil.isNotBlank(led.getSoundText()) ? ledLineVO.getSoundText() : "");
        return ledLineVO;
    }
}
