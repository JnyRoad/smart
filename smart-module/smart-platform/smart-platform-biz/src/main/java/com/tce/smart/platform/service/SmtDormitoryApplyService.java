package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryApplyFailBackDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryApplyReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryApplySearchReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.SearchCommonSDRecordRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryApplySearchRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryDistRespDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryApply;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 内宿申请
 *
 */
public interface SmtDormitoryApplyService extends IService<SmtDormitoryApply> {

	/**
	 * 分页查询内宿申请记录
	 * @param searchReqDTO
	 * @return
	 */
	IPage<DormitoryApplySearchRespDTO> getApplyRecord(DormitoryApplySearchReqDTO searchReqDTO);

	/**
	 * 申请内宿
	 * @param applyReqDTO
	 * @return
	 */
	Boolean saveApply(DormitoryApplyReqDTO applyReqDTO);

	/**
	 * 撤销申请
	 * @return
	 */
	Boolean cancelApply();

	/**
	 * 退回申请
	 * @param failBackDTO
	 * @return
	 */
	Boolean failbackApply(DormitoryApplyFailBackDTO failBackDTO);

	/**
	 * 手动分配宿舍
	 * @param applyId
	 * @param bedId
	 * @return
	 */
	Boolean manualDis(Long applyId, Integer bedId, SmtDormitoryStaffService smtDormitoryStaffService);

	/**
	 * 自动分配宿舍
	 * @param applyId
	 * @return
	 */
	DormitoryDistRespDTO recommendDis(Long applyId);
}
