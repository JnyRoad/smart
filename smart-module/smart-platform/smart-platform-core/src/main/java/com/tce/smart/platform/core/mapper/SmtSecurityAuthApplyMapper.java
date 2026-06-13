package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthApplyPageRespDTO;
import com.tce.smart.platform.core.ao.SecurityAuthApplyPageQueryAO;
import com.tce.smart.platform.core.dto.SecurityAuthApplyPageDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import org.apache.ibatis.annotations.Param;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:31
 */
public interface SmtSecurityAuthApplyMapper extends BaseMapper<SmtSecurityAuthApply> {

	/**
	 * 获得分页列表
	 * @param query
	 * @return
	 */
	IPage<SecurityAuthApplyPageRespDTO> getPage(Page page, @Param("query") SecurityAuthApplyPageQueryAO query);

}
