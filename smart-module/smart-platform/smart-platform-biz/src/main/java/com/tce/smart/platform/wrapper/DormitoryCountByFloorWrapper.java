package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.model.DormitoryCountByFloor;
import com.tce.smart.platform.core.vo.DormitoryCountByFloorVO;
import com.tce.smart.tool.enums.DormitorySexEnum;

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
public class DormitoryCountByFloorWrapper extends BaseWrapper<DormitoryCountByFloor, DormitoryCountByFloorVO> {
    @Override
    protected DormitoryCountByFloorVO warp(DormitoryCountByFloor dormitoryCountByFloor) throws IOException {
        DormitoryCountByFloorVO dormitoryCountByFloorVO = new DormitoryCountByFloorVO();
        BeanUtil.copyProperties(dormitoryCountByFloor, dormitoryCountByFloorVO);
        dormitoryCountByFloorVO.setSex(DormitorySexEnum.desc(dormitoryCountByFloor.getRoomSex()));
        return dormitoryCountByFloorVO;
    }
}
