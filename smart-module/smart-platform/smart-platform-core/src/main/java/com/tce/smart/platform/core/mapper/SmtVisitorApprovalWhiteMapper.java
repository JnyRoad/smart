package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.VisitorWhiteDTO;
import com.tce.smart.platform.core.entity.SmtVisitorApprovalWhite;
import org.apache.ibatis.annotations.Param;

/**
 * @description: SmtDormitoryApplyMapper
 * @date: 2020-12-29
 * @author: wuling
 * @version: 1.0
 */
public interface SmtVisitorApprovalWhiteMapper extends BaseMapper<SmtVisitorApprovalWhite> {
	IPage<VisitorWhiteDTO> pageQuery(@Param("page") Page page, @Param("query")VisitorWhiteDTO visitorWhiteDTO);
}
