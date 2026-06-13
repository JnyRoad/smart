package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtOutSrcApplyDetails;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/2 16:56
 */
public interface SmtOutSrcApplyDetailsService extends IService<SmtOutSrcApplyDetails> {

	/**
	 * 通过申请单ID获取明细
	 * @param page
	 * @param applyId
	 * @return
	 */
	IPage<SmtOutSrcApplyDetails> getByApplyId(Page page, Long applyId);

	/**
	 * 通过申请单ID获取明细
	 * @param applyId
	 * @return
	 */
	List<SmtOutSrcApplyDetails> getByApplyId(Long applyId);

	/**
	 * 根据工号和申请单号查询
	 * @param badge
	 * @param applyIds
	 * @return
	 */
	SmtOutSrcApplyDetails getByBadgeAndApplyId(String badge, List<Long> applyIds);
}
