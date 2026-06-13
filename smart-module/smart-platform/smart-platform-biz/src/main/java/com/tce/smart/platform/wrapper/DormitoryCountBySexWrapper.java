package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.model.DormitoryCountBySex;
import com.tce.smart.platform.core.vo.DormitoryCountBySexVO;
import com.tce.smart.tool.enums.DormitorySexEnum;

import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountBySexWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class DormitoryCountBySexWrapper extends BaseWrapper<DormitoryCountBySex, DormitoryCountBySexVO> {
    @Override
    protected DormitoryCountBySexVO warp(DormitoryCountBySex dormitoryCountBySex) throws IOException {
        DormitoryCountBySexVO dormitoryCountBySexVO = new DormitoryCountBySexVO();
        BeanUtil.copyProperties(dormitoryCountBySex, dormitoryCountBySexVO);
        dormitoryCountBySexVO.setSex(DormitorySexEnum.desc(dormitoryCountBySex.getRoomSex()));
        return dormitoryCountBySexVO;
    }
}
