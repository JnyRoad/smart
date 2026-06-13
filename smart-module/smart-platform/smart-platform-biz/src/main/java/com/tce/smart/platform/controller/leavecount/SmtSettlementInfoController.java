package com.tce.smart.platform.controller.leavecount;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementCountReqDTO;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementInfoQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementInfoDhrRespDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementInfoRespDTO;
import com.tce.smart.platform.service.leavecount.SmtSettlementInfoService;
import com.tce.smart.platform.service.leavecount.SmtSettlementLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

/**
 * @author fushiping
 * @date 2022-06-21 11:02:12
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-离职水电结算生成记录")
@RequestMapping("/settlement")
public class SmtSettlementInfoController extends BaseController {

	private final SmtSettlementInfoService smtSettlementInfoService;

	private final SmtSettlementLogService smtSettlementLogService;

	/**
	 * 分页查询
	 *
	 * @param page 分页对象
	 * @return
	 */
	@ApiOperation("分页查询")
	@GetMapping("/page")
	public Result<IPage<SettlementInfoRespDTO>> getPage(Page page, SettlementInfoQueryReqDTO queryReqDTO) {
		return success(smtSettlementInfoService.getPage(page, queryReqDTO), SettlementInfoRespDTO.class);
	}

	/**
	 * @param smtSettlementInfo
	 * @return Result
	 */
	@SysLog("离职水电结算")
	@ApiOperation("离职水电结算")
	@PostMapping("/count")
	public Result<SettlementInfoDhrRespDTO> count(@RequestBody SettlementCountReqDTO smtSettlementInfo) {
		Result<SettlementInfoDhrRespDTO> result = success(smtSettlementInfoService.getSettlement(smtSettlementInfo));
		smtSettlementLogService.saveLog(result, smtSettlementInfo);
		return result;
	}


}
