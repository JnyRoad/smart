package com.tce.smart.platform.controller.leavecount;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementLogRespDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementLog;
import com.tce.smart.platform.service.leavecount.SmtSettlementLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import java.math.BigDecimal;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:02:04
 */
@RestController
@Api(tags = "platform-离职水电结算日志查询")
@AllArgsConstructor
@RequestMapping("/settlement/log")
public class SmtSettlementLogController extends BaseController {

  private final SmtSettlementLogService smtSettlementLogService;

  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @ApiOperation("通过任务id查询日志")
  @GetMapping("/{id}")
  public Result<SettlementLogRespDTO> getById(@PathVariable("id") String id){
    return success(smtSettlementLogService.getOne(Wrappers.<SmtSettlementLog>lambdaQuery()
			.eq(SmtSettlementLog::getInfoId, Long.parseLong(id))), SettlementLogRespDTO.class);
  }

}
