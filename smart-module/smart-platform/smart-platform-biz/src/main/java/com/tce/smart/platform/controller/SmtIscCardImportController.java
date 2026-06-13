package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportBatchPageReqDTO;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportDetailPageReqDTO;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportStartReqDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscCardImportBatchRespDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscCardImportDetailRespDTO;
import com.tce.smart.platform.core.entity.SmtIscCardImportBatch;
import com.tce.smart.platform.core.enums.IscCardImportModeEnum;
import com.tce.smart.platform.service.isc.SmtIscCardImportService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/isc/card/import")
public class SmtIscCardImportController extends BaseController {

	private final SmtIscCardImportService smtIscCardImportService;

	@SysLog("预检海康ISC卡片初始化同步")
	@PostMapping("/dry-run")
	public Result dryRun(@Valid @RequestBody IscCardImportStartReqDTO reqDTO) {
		SmtIscCardImportBatch batch = smtIscCardImportService.createBatch(reqDTO,
				IscCardImportModeEnum.DRY_RUN.getCode(), allowedParkIds());
		smtIscCardImportService.executeBatch(batch.getId());
		return success(batch, IscCardImportBatchRespDTO.class);
	}

	@SysLog("执行海康ISC卡片初始化导入")
	@PostMapping("/import")
	public Result importCards(@Valid @RequestBody IscCardImportStartReqDTO reqDTO) {
		SmtIscCardImportBatch batch = smtIscCardImportService.createBatch(reqDTO,
				IscCardImportModeEnum.IMPORT.getCode(), allowedParkIds());
		smtIscCardImportService.executeBatch(batch.getId());
		return success(batch, IscCardImportBatchRespDTO.class);
	}

	@GetMapping("/batch/page")
	public Result getBatchPage(Page page, IscCardImportBatchPageReqDTO query) {
		IscCardImportBatchPageReqDTO pageQuery = query == null ? new IscCardImportBatchPageReqDTO() : query;
		pageQuery.setParkIds(allowedParkIds());
		return success(smtIscCardImportService.getBatchPage(page, pageQuery), IscCardImportBatchRespDTO.class);
	}

	@GetMapping("/detail/page")
	public Result getDetailPage(Page page, IscCardImportDetailPageReqDTO query) {
		IscCardImportDetailPageReqDTO pageQuery = query == null ? new IscCardImportDetailPageReqDTO() : query;
		pageQuery.setParkIds(allowedParkIds());
		return success(smtIscCardImportService.getDetailPage(page, pageQuery), IscCardImportDetailRespDTO.class);
	}

	private List<Integer> allowedParkIds() {
		return SecurityUtils.getUser().getParkIdList();
	}
}
