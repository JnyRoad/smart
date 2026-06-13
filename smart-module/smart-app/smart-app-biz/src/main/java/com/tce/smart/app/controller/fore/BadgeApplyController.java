package com.tce.smart.app.controller.fore;


import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.badge.EditBadgeApplyReqDTO;
import com.tce.smart.platform.api.feign.badge.RemoteBadgeApplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 厂牌补领
 *
 * @author fushiping
 * @date 2020-07-07 11:47:58
 */
@RestController
@AllArgsConstructor
@RequestMapping("/badge/apply")
@Api(tags = "厂牌补领")
public class BadgeApplyController extends BaseController {

  private final RemoteBadgeApplyService remoteBadgeApplyService;


  /**
   * 通过id查询厂牌补领
   * @param id id
   * @return Result
   */
  @GetMapping("/detail")
  @ApiOperation("通过id查询厂牌补领")
  public Result getById(@RequestParam("id") Long id){
    return success(remoteBadgeApplyService.getById(id, SecurityConstants.FROM_IN));
  }

  /**
   * 新增厂牌补领
   * @param reqDTO 厂牌补领
   * @return Result
   */
  @SysLog("新增厂牌补领")
  @PostMapping("/save")
  @ApiOperation("新增厂牌补领")
  public Result save(@RequestBody EditBadgeApplyReqDTO reqDTO){
	reqDTO.setStaffNo(SecurityUtils.getUser().getUsername());
    return success(remoteBadgeApplyService.save(reqDTO, SecurityConstants.FROM_IN));
  }
}
