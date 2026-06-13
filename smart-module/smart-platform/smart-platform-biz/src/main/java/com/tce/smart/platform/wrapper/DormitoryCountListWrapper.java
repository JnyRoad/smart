package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.DormitoryFloorRespDTO;
import com.tce.smart.platform.core.entity.SmtDormitory;
import com.tce.smart.platform.core.model.DormitoryCountBuilding;
import com.tce.smart.platform.core.model.DormitoryCountFloor;
import com.tce.smart.platform.core.model.DormitoryCountList;
import com.tce.smart.platform.core.vo.DormitoryCountListByBuildingVO;
import com.tce.smart.platform.core.vo.DormitoryCountListByFloorVO;
import com.tce.smart.platform.core.vo.DormitoryCountListVO;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import com.tce.smart.platform.service.SmtDormitoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountListWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class DormitoryCountListWrapper extends BaseWrapper<DormitoryCountList, DormitoryCountListVO> {
    @Autowired
    private SmtDormitoryService dormitoryService;
    @Autowired
    private SmtDormitoryRoomService dormitoryRoomService;
    @Override
    protected DormitoryCountListVO warp(DormitoryCountList dormitoryCountList) throws IOException {
        DormitoryCountListVO dormitoryCountListVO = new DormitoryCountListVO();
        BeanUtil.copyProperties(dormitoryCountList, dormitoryCountListVO);

        List<DormitoryFloorRespDTO> dormitoryList = dormitoryService.getByParkId(dormitoryCountList.getParkId(), null);
        if(CollectionUtils.isNotEmpty(dormitoryList)){
            dormitoryList.forEach(dormitory -> dormitoryCountListVO.setDormitoryCountListByBuildingVOList(countDormitory(dormitory.getParkId())));
        }
        return dormitoryCountListVO;
    }

    private List<DormitoryCountListByBuildingVO> countDormitory(Integer parkId){
        List<DormitoryCountListByBuildingVO> dormitoryCountListByBuildingVOList = new ArrayList<>();
        List<DormitoryCountBuilding> dormitoryCountBuildingList = dormitoryRoomService.countBuilding(parkId);
        if(CollectionUtils.isNotEmpty(dormitoryCountBuildingList)){
            dormitoryCountBuildingList.forEach(d -> {
                DormitoryCountListByBuildingVO dormitoryCountListByBuildingVO = new DormitoryCountListByBuildingVO();
                BeanUtil.copyProperties(d, dormitoryCountListByBuildingVO);
                dormitoryCountListByBuildingVO.setDormitoryCountListByFloorVOList(countFloor(d.getDormitoryId()));
                //剩余数量
                dormitoryCountListByBuildingVO.setSurplus(d.getTotal() - d.getManNumber() - d.getWomanNumber());
                dormitoryCountListByBuildingVOList.add(dormitoryCountListByBuildingVO);
            });
        }
        return dormitoryCountListByBuildingVOList;
    }

    private List<DormitoryCountListByFloorVO> countFloor(Integer dormitoryId){
        List<DormitoryCountListByFloorVO> dormitoryCountListByFloorVOList = new ArrayList<>();
        List<DormitoryCountFloor> dormitoryCountFloorList = dormitoryRoomService.countFloor(dormitoryId);
        if(CollectionUtils.isNotEmpty(dormitoryCountFloorList)){
            dormitoryCountFloorList.forEach(d -> {
                DormitoryCountListByFloorVO dormitoryCountListByFloorVO = new DormitoryCountListByFloorVO();
                BeanUtil.copyProperties(d, dormitoryCountListByFloorVO);

                //剩余数量
                dormitoryCountListByFloorVO.setSurplus(d.getTotal() - d.getManNumber() - d.getWomanNumber());
                dormitoryCountListByFloorVOList.add(dormitoryCountListByFloorVO);
            });
        }
        return dormitoryCountListByFloorVOList;
    }
}
