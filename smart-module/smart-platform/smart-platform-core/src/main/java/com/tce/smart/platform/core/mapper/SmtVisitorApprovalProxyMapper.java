package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.VisitorProxyDTO;
import com.tce.smart.platform.core.dto.VisitorWhiteDTO;
import com.tce.smart.platform.core.entity.SmtVisitorApprovalProxy;
import org.apache.ibatis.annotations.Param;

/**
 * @description: SmtVisitorApprovalProxyMapper
 * @date: 2020-12-29
 * @author: wuling
 * @version: 1.0
 */
public interface SmtVisitorApprovalProxyMapper extends BaseMapper<SmtVisitorApprovalProxy> {
	IPage<VisitorProxyDTO> pageQuery(@Param("page") Page page, @Param("query") VisitorProxyDTO visitorProxyDTO);
}
