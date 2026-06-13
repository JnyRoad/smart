package com.tce.smart.platform.service.settlement;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.sddto.SaveCommonSDMeterreadReqDTO;
import com.tce.smart.platform.api.dto.req.sddto.SearchCommonSDMeterreadReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.CommonSDMeterreadRespDTO;
import com.tce.smart.platform.core.entity.SmtCommonSDMeterread;

/**
 * 公摊水电抄表
 *
 */
public interface SmtCommonSDMeterreadService extends IService<SmtCommonSDMeterread> {

	/**
	 * 按收费项目查询所有公摊水电表的抄表历史记录
	 * @return
	 */
	Page<CommonSDMeterreadRespDTO> getCommonSDMeterreadHisByCate(SearchCommonSDMeterreadReqDTO searchCommonSDMeterreadReqDTO,SmtCommonSDService smtCommonSDService);

	/**
	 * 查询公摊水电表的抄表历史记录
	 * @param page
	 * @param commId
	 * @return
	 */
	Page<CommonSDMeterreadRespDTO> getCommonSDMeterreadHis(Page page,Long commId);

	/**
	 * 查询公摊水电抄表记录
	 * @param searchCommonSDMeterreadReqDTO
	 * @return
	 */
	CommonSDMeterreadRespDTO getCommonSDMeterread(SearchCommonSDMeterreadReqDTO searchCommonSDMeterreadReqDTO);

	/**
	 * 添加公摊水电超表记录
	 * @param saveCommonSDMeterreadReqDTO
	 * @return
	 */
	Boolean saveCommonSDMeterread(SaveCommonSDMeterreadReqDTO saveCommonSDMeterreadReqDTO);

	/**
	 * 删除公摊水电抄表记录
	 * @param id
	 * @return
	 */
	Boolean delCommonSDMeterread(Long id);

}
