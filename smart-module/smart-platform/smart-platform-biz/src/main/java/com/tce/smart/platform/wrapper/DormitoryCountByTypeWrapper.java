package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.model.DormitoryCountByType;
import com.tce.smart.platform.core.vo.DormitoryCountByTypeVO;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByTypeWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class DormitoryCountByTypeWrapper extends BaseWrapper<DormitoryCountByType, DormitoryCountByTypeVO> {
    @Override
    protected DormitoryCountByTypeVO warp(DormitoryCountByType dormitoryCountByType) throws IOException {
        DormitoryCountByTypeVO dormitoryCountByTypeVO = new DormitoryCountByTypeVO();
        BeanUtil.copyProperties(dormitoryCountByType, dormitoryCountByTypeVO);
        return dormitoryCountByTypeVO;
    }
}
