package com.tce.smart.platform.service.settlement;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.sddto.AddCommonSDReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.SearchCommonSDRecordRespDTO;
import com.tce.smart.platform.core.entity.SmtCommonSD;

/**
 * 公摊水电
 *
 */
public interface SmtCommonSDService extends IService<SmtCommonSD> {

	/**
	 * 按收费类型查询公共水电记录
	 * @param page
	 * @return
	 */
	IPage<SearchCommonSDRecordRespDTO> getCommonSDCategoryRecord(Page page,Integer categoryId);

	/**
	 * 添加公摊水电表记录
	 * @param addCommonSDReqDTO
	 * @return
	 */
	Boolean saveCommonSDRecord(AddCommonSDReqDTO addCommonSDReqDTO);

	/**
	 * 删除公摊水电表记录
	 * @param id
	 * @return
	 */
	Boolean delCommonSDRecord(Long id);

}
