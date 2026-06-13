package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportBatchPageReqDTO;
import com.tce.smart.platform.core.entity.SmtIscCardImportBatch;
import org.apache.ibatis.annotations.Param;

public interface SmtIscCardImportBatchMapper extends BaseMapper<SmtIscCardImportBatch> {

	SmtIscCardImportBatch getById(@Param("id") Long id);

	IPage<SmtIscCardImportBatch> getPage(Page page, @Param("query") IscCardImportBatchPageReqDTO query);
}
