package com.tce.smart.platform.service.isc;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.isc.IscAccessCleanupExecuteReqDTO;
import com.tce.smart.platform.api.dto.req.isc.IscAccessCleanupPageReqDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscAccessCleanupExecuteRespDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscAccessCleanupSummaryRespDTO;
import com.tce.smart.platform.core.vo.IscAccessCleanupRecordVO;

import java.util.List;

public interface SmtIscAccessCleanupService {

	IPage<IscAccessCleanupRecordVO> getPage(Page page, IscAccessCleanupPageReqDTO query, List<Integer> allowedParkIds);

	IscAccessCleanupSummaryRespDTO getSummary(IscAccessCleanupPageReqDTO query, List<Integer> allowedParkIds);

	IscAccessCleanupExecuteRespDTO execute(IscAccessCleanupExecuteReqDTO reqDTO, List<Integer> allowedParkIds);
}
