package com.tce.smart.platform.service.badge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.badge.EditBadgeApplyReqDTO;
import com.tce.smart.platform.api.dto.req.badge.QueryApplyListReqDTO;
import com.tce.smart.platform.core.entity.badge.SmtBadgeApply;
import org.springframework.http.ResponseEntity;

/**
 * 厂牌补领
 *
 * @author fushiping
 * @date 2020-07-07 11:47:58
 */
public interface SmtBadgeApplyService extends IService<SmtBadgeApply> {

	IPage<SmtBadgeApply> getPage(Page page, QueryApplyListReqDTO reqDTO);

	/**
	 * 新增厂牌补领记录
	 * @param reqDTO
	 * @return
	 */
	Boolean saveBadgeApply(EditBadgeApplyReqDTO reqDTO);

	/**
	 * 编辑厂牌补领记录
	 * @param reqDTO
	 * @return
	 */
	Boolean updateBadgeApply(EditBadgeApplyReqDTO reqDTO);

	/**
	 * 补领记录导出
	 * @return
	 */
	ResponseEntity<byte[]> downLoadExcel(SmtBadgeApply smtBadgeLoss);

}
