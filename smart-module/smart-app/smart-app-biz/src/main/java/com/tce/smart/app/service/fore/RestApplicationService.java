package com.tce.smart.app.service.fore;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.vo.fore.AdjustVo;
import com.tce.smart.app.vo.fore.RestDetailVo;
import com.tce.smart.app.vo.fore.RestTypeVo;
import com.tce.smart.platform.api.dto.req.AddBreakOffApplicationReqDTO;

import java.util.Map;

/**
 * 调休申请接口
 * @author 梁圆
 *
 */
public interface RestApplicationService {

	/**
	 * 获取调休类型
	 * @return
	 */
	RestTypeVo getRestType();

	 /**
	  * 获取调休的列表
	  * @param params
	  * @return
	  */
	 Page<?> getRestList(Map<String, Object> params,String staffBadge);


	 /**
	  * 获取调休的详情
	  * @param allApplicationAoId
	  * @return
	  */
	 RestDetailVo getRestDetail(AllApplicationAo allApplicationAoId);

	 /**
	  * 添加调休申请
	  * @param addBreakOffApplicationDTO
	  */
	void addRest(AddBreakOffApplicationReqDTO addBreakOffApplicationDTO);

	/**
	 * 获取员工可调休的天数
	 * @return
	 */
	AdjustVo getAdjust(String staffBadge);
}
