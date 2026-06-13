package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.isc.IscAccessCleanupPageReqDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscAccessCleanupSummaryRespDTO;
import com.tce.smart.platform.core.vo.IscAccessCleanupRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SmtIscAccessCleanupMapper {

	IPage<IscAccessCleanupRecordVO> getPage(Page page, @Param("query") IscAccessCleanupPageReqDTO query,
											@Param("now") Date now);

	IscAccessCleanupSummaryRespDTO getSummary(@Param("query") IscAccessCleanupPageReqDTO query,
											  @Param("now") Date now);

	List<IscAccessCleanupRecordVO> listRecords(@Param("query") IscAccessCleanupPageReqDTO query,
											   @Param("now") Date now, @Param("limit") Integer limit);
}
