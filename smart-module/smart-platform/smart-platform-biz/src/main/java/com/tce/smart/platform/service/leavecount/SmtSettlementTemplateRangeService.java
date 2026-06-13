package com.tce.smart.platform.service.leavecount;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateRangeReqDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementTemplateRangeTreeRespDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateRange;

import java.util.List;

/**
 * @author fushiping
 * @date 2022-06-21 11:01:50
 */
public interface SmtSettlementTemplateRangeService extends IService<SmtSettlementTemplateRange> {

	/**
	 * 批量编辑模板范围
	 *
	 * @param reqDTO
	 * @return
	 */
	Boolean editRangeBatch(List<SettlementTemplateRangeReqDTO> reqDTO);

	/**
	 * 编辑房间模板范围
	 *
	 * @param reqDTO
	 * @return
	 */
	Boolean editRangeSingle(SettlementTemplateRangeReqDTO reqDTO);

	/**
	 * 通过房间id获取模板id列表
	 *
	 * @param roomId
	 * @return
	 */
	List<Long> getByRoomId(Integer roomId);

	/**
	 * 查询宿舍树
	 *
	 * @param parkId
	 * @param tempId
	 * @param type
	 * @return
	 */
	List<SettlementTemplateRangeTreeRespDTO> getRangeTree(Integer parkId, Long tempId, String type);
}
