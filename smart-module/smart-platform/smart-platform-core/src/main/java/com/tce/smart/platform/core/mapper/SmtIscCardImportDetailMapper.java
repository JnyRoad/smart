package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportDetailPageReqDTO;
import com.tce.smart.platform.core.entity.SmtIscCardImportDetail;
import org.apache.ibatis.annotations.Param;

public interface SmtIscCardImportDetailMapper extends BaseMapper<SmtIscCardImportDetail> {

	IPage<SmtIscCardImportDetail> getPage(Page page, @Param("query") IscCardImportDetailPageReqDTO query);
}
