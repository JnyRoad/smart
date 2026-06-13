package com.tce.smart.data.service.dhr;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.data.api.dto.dhrview.resp.YutoDhrPsndoDTO;
import com.tce.smart.dhrview.core.model.YutoDhrPsndoCoreDTO;

import java.util.List;

/**
 * @description: YutoDhrPsndoService
 * @date: 2021/5/27 0027 15:48
 * @author: wuling
 * @version: 1.0
 */
public interface DhrPsndoService {
	Page<YutoDhrPsndoDTO> getPage(Page page, List<Integer> buIds);

	/**
	 * 获得员工性质
	 * @param badge
	 * @return
	 */
	String getProperties(String badge);

	/**
	 * 根据userId获取工号
	 * @param userId
	 * @return
	 */
	String getBadgeByUserId(String userId);
}
