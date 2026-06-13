package com.tce.smart.platform.service.isc;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportBatchPageReqDTO;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportDetailPageReqDTO;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportStartReqDTO;
import com.tce.smart.platform.core.entity.SmtIscCardImportBatch;
import com.tce.smart.platform.core.entity.SmtIscCardImportDetail;

import java.util.List;

public interface SmtIscCardImportService {

	SmtIscCardImportBatch createBatch(IscCardImportStartReqDTO reqDTO, String mode, List<Integer> allowedParkIds);

	void executeBatch(Long batchId);

	IPage<SmtIscCardImportBatch> getBatchPage(Page page, IscCardImportBatchPageReqDTO query);

	IPage<SmtIscCardImportDetail> getDetailPage(Page page, IscCardImportDetailPageReqDTO query);
}
