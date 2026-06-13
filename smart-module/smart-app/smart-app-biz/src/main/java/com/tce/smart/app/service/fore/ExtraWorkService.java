package com.tce.smart.app.service.fore;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.vo.fore.ExtraWorkClassVo;
import com.tce.smart.app.vo.fore.ExtraWorkDetailVo;
import com.tce.smart.app.vo.fore.ExtraWorkTypeVo;
import com.tce.smart.platform.api.dto.req.AddOverTimeApplicationReqDTO;

import java.util.Map;

/**
 * 加班申请接口
 * @author 梁圆
 *
 */
public interface ExtraWorkService {

	/**
	 * 获取加班类型
	 * @return
	 */
	ExtraWorkTypeVo getExtraWorkType();

	/**
	 * 获取加班班别
	 * @return
	 */
	ExtraWorkClassVo getExtraClassType();

	/**
	 * 获取加班列表
	 * @param params
	 * @return
	 */
	Page<?> getExtraWorkList(Map<String, Object> params);

	/**
	 * 获取加班的详情
	 * @param vacateAoId
	 * @return
	 */
	ExtraWorkDetailVo getExtraWorkDetail(AllApplicationAo vacateAoId);

	/**
	 * 添加加班申请
	 * @param addOverTimeApplicationDTO
	 */
	void addExtraWork(AddOverTimeApplicationReqDTO addOverTimeApplicationDTO);
}
