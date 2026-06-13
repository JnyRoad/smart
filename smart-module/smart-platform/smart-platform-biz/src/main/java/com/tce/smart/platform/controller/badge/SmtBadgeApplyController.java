package com.tce.smart.platform.controller.badge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.badge.EditBadgeApplyReqDTO;
import com.tce.smart.platform.api.dto.req.badge.QueryApplyListReqDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeApplyDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeApplyListRespDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeApplyRecordRespDTO;
import com.tce.smart.platform.core.entity.badge.SmtBadgeApply;
import com.tce.smart.platform.service.badge.SmtBadgeApplyService;
import com.tce.smart.tool.enums.BadgeApplyReasonEnum;
import com.tce.smart.tool.enums.BadgeOperaStatusEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import java.util.List;
import java.util.Map;

/**
 * 厂牌补领
 *
 * @author fushiping
 * @date 2020-07-07 11:47:58
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-厂牌补领")
@RequestMapping("/badge/apply")
public class SmtBadgeApplyController extends BaseController {

  private final SmtBadgeApplyService smtBadgeApplyService;

  /**
   * 分页查询
   * @param page 分页对象
   * @return
   */
  @PostMapping("/page")
  public Result getPage(Page page, @RequestBody(required = false) QueryApplyListReqDTO reqDTO) {
    return success(smtBadgeApplyService.getPage(page,reqDTO), BadgeApplyListRespDTO.class);
  }


  /**
   * 通过id查询厂牌补领
   * @param id id
   * @return Result
   */
  @GetMapping("/detail")
  public Result getById(@RequestParam("id") Long id){
    return success(smtBadgeApplyService.getById(id), BadgeApplyDetailRespDTO.class);
  }

	/**
	 * 查询当前登录对象申请记录
	 * @param page 分页对象
	 * @return
	 */
	@GetMapping("/inner/page")
	public Result<IPage<BadgeApplyRecordRespDTO>> getUserRecord(Page page) {
		String staffNo = SecurityUtils.getUser().getUsername();
		return success(smtBadgeApplyService.page(page,Wrappers.<SmtBadgeApply>query().lambda()
				.eq(SmtBadgeApply::getBadge, staffNo)
				.orderByDesc(SmtBadgeApply::getCreateTime)), BadgeApplyRecordRespDTO.class);
	}

  /**
   * 新增厂牌补领
   * @param reqDTO 厂牌补领
   * @return Result
   */
  @SysLog("新增厂牌补领")
  @PostMapping("/save")
  public Result save(@RequestBody EditBadgeApplyReqDTO reqDTO){
    return success(smtBadgeApplyService.saveBadgeApply(reqDTO));
  }

  /**
   * 修改厂牌补领
   * @param reqDTO 厂牌补领
   * @return Result
   */
  @ApiOperation("修改厂牌补领")
  @SysLog("修改厂牌补领")
  @PostMapping("/update")
  public Result updateById(@RequestBody EditBadgeApplyReqDTO reqDTO){
    return success(smtBadgeApplyService.updateBadgeApply(reqDTO));
  }

	/**
	 * 挂失记录导出
	 * @param
	 * @return
	 */
	@ApiOperation("挂失记录导出")
	@PostMapping("/excel")
	public ResponseEntity<byte[]> excel(@RequestBody(required = false) QueryApplyListReqDTO reqDTO) {
		SmtBadgeApply smtBadgeLoss = BeanUtils.transform(SmtBadgeApply.class, reqDTO);
		return smtBadgeApplyService.downLoadExcel(smtBadgeLoss);
	}

	/**
	 * 状态枚举
	 * @return Result
	 */
	@ApiOperation("操作状态枚举")
	@GetMapping("/enum/status")
	public Result<List<Map<String, Object>>> getOperaStatus(){
		return success(BadgeOperaStatusEnum.list());
	}

	/**
	 * 状态枚举
	 * @return Result
	 */
	@ApiOperation("补领申请原因枚举")
	@GetMapping("/enum/reason/status")
	public Result<List<Map<String, Object>>> getApplyReason(){
		return success(BadgeApplyReasonEnum.list());
	}

}
