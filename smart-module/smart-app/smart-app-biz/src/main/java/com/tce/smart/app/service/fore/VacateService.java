package com.tce.smart.app.service.fore;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.ao.fore.VacateClassAo;
import com.tce.smart.app.vo.fore.VacateClassVo;
import com.tce.smart.app.vo.fore.VacateDetailVo;
import com.tce.smart.app.vo.fore.VacateTypeVo;
import com.tce.smart.app.vo.fore.VacateUnitVo;
import com.tce.smart.platform.api.dto.req.AddAskLeavelApplicationReqDTO;

import java.util.Map;

/**
 * 请假申请接口
 * @author 梁圆
 *
 */
public interface VacateService {

	/**
	 * 获取请假类型
	 * @return
	 */
	VacateTypeVo getVacateType();

	/**
	 * 获取请假类表
	 * @param params
	 * @return
	 */
	Page<?> getVacateList(Map<String, Object> params);

	/**
	 * 获取请假的详情
	 * @param vacateAoId
	 * @return
	 */
	VacateDetailVo getVacateDetail(AllApplicationAo vacateAoId);

	/**
	 * 添加请假申请
	 * @param addAskLeavelApplicationDTO
	 */
	void addVacate(AddAskLeavelApplicationReqDTO addAskLeavelApplicationDTO);

	/**
	 * 查询班次
	 * @param vacateClassVo
	 * @return
	 */
	VacateClassVo getVacateClasses(VacateClassAo vacateClassVo);

	VacateUnitVo getUnitByVacateType(String vacateCode);
}
