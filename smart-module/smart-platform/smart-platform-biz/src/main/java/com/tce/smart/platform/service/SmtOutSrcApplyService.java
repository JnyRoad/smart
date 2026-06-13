package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.TempStaffEditReqDTO;
import com.tce.smart.platform.api.dto.req.outsrcapply.SmtOutSrcApplyReqDTO;
import com.tce.smart.platform.api.dto.req.outsrcapply.SmtOutSrcApplyUpdDTO;
import com.tce.smart.platform.api.dto.resp.outsrcapply.SmtOutSrcApplyDetailListDTO;
import com.tce.smart.platform.api.dto.resp.outsrcapply.SmtOutSrcApplyDetailRespDTO;
import com.tce.smart.platform.core.entity.SmtOutSrcApply;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/2 16:54
 */
public interface SmtOutSrcApplyService extends IService<SmtOutSrcApply> {
	/**
	 * 新增审批单
	 * @param tempStaffs
	 * @return
	 */
	Boolean saveBatchRec(List<TempStaffEditReqDTO> tempStaffs);

	/**
	 * 分页获取申请单信息
	 * @param page
	 * @param applyReqDTO
	 * @return
	 */
	IPage<SmtOutSrcApply> getPage(Page page, SmtOutSrcApplyReqDTO applyReqDTO);

	/**
	 * 根据申请单id获取详情
	 * @param applyId
	 * @return
	 */
	SmtOutSrcApplyDetailRespDTO getDetail(Long applyId);

	/**
	 * 根据申请单id获取详情
	 * @param page
	 * @param applyId
	 * @return
	 */
	IPage<SmtOutSrcApplyDetailListDTO> getDetailPage(Page page, Long applyId);

	/**
	 * 通过申请单/拒绝申请单
	 * @param dto
	 * @return
	 */
	Boolean passOrRefuse(SmtOutSrcApplyUpdDTO dto, SmtDormitoryStaffService smtDormitoryStaffService);
}
