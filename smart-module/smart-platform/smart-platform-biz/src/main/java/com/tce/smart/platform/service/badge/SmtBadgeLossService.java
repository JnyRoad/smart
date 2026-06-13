package com.tce.smart.platform.service.badge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.data.api.dto.consume.resp.RsEmpRespDTO;
import com.tce.smart.platform.api.dto.req.badge.QueryLossInfoReqDTO;
import com.tce.smart.platform.core.entity.badge.SmtBadgeLoss;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 厂牌挂失
 *
 * @author fushiping
 * @date 2020-07-07 11:47:43
 */
public interface SmtBadgeLossService extends IService<SmtBadgeLoss> {

	/**
	 * 分页查询挂失记录
	 * @param page
	 * @param reqDTO
	 * @return
	 */
	IPage<SmtBadgeLoss> getPage(Page page, QueryLossInfoReqDTO reqDTO);

	/**
	 * 厂牌挂失
	 * @param parkId 园区id
	 * @param staffNo 员工工号
	 * @return
	 */
	Boolean saveBadgeLoss(Integer parkId, String staffNo);

	/**
	 * excel下载
	 * @return
	 */
	ResponseEntity<byte[]> downLoadExcel(SmtBadgeLoss smtBadgeLoss);

}
