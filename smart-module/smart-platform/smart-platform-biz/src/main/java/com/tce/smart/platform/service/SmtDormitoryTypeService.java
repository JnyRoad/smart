package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.DormitoryTypeRespDTO;
import com.tce.smart.platform.core.dto.DormitoryTypeDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryType;
import com.tce.smart.platform.core.vo.DormitoryTypeVO;

import java.util.List;

/**
 * 园区宿舍类型
 *
 * @author 齐佩
 * @date 2019-04-13 18:16:57
 */
public interface SmtDormitoryTypeService extends IService<SmtDormitoryType> {

	Result removeDormitoryTypeById(Integer id);

	Result updateDormitoryTypeById(DormitoryTypeDTO smtDormitoryType);

	Result addDormitoryType(DormitoryTypeDTO smtDormitoryType);

	IPage<DormitoryTypeVO> getSmtDormitoryTypePage(Page page, SmtDormitoryType smtDormitoryType);

	Result getSmtDormitoryTypeAll();

	List<DormitoryTypeRespDTO> getSmtDormitoryTypeByPark(Integer parkId);

	List<DormitoryTypeRespDTO> getSmtDormitoryTypeByParkAndDormitory(Integer parkId,Integer dormitoryId);

}
