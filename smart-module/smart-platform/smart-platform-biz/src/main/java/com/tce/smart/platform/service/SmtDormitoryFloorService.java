package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.DormitoryFloorReqDTO;
import com.tce.smart.platform.core.dto.DormitoryFloorDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryFloor;

import java.util.List;

/**
 * 园区宿舍楼的楼层
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:15
 */
public interface SmtDormitoryFloorService extends IService<SmtDormitoryFloor> {

	Result updateDormitoryFloorById(SmtDormitoryFloor smtDormitoryFloor);

	Result<List<SmtDormitoryFloor>> queryFloor(DormitoryFloorReqDTO smtDormitoryFloor);

	Result addFloor(DormitoryFloorDTO dormitoryFloorDTO);

	Result removeFloorById(Integer id);

	Result getSmtDormitoryFloorPage(Page page, SmtDormitoryFloor smtDormitoryFloor);

	Result getFloorStartNum(Integer dormitoryId);

}
