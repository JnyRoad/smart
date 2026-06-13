package com.tce.smart.platform.controller.badge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.req.badge.QueryLossInfoReqDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeLossInfoRespDTO;
import com.tce.smart.platform.core.entity.badge.SmtBadgeLoss;
import com.tce.smart.platform.service.badge.SmtBadgeLossService;
import com.tce.smart.tool.enums.BadgeOperaStatusEnum;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import java.util.List;
import java.util.Map;

/**
 * 厂牌挂失
 *
 * @author fushiping
 * @date 2020-07-07 11:47:43
 */
@RestController
@AllArgsConstructor
@RequestMapping("/badge/loss")
public class SmtBadgeLossController extends BaseController {

  private final SmtBadgeLossService smtBadgeLossService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param reqDTO 厂牌挂失
   * @return
   */
  @PostMapping("/page")
  public Result<IPage<BadgeLossInfoRespDTO>> getSmtBadgeLossPage(Page page, @RequestBody(required = false) QueryLossInfoReqDTO reqDTO) {
      return success(smtBadgeLossService.getPage(page, reqDTO), BadgeLossInfoRespDTO.class);
  }

  /**
   * 通过id查询厂牌挂失
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtBadgeLossService.getById(id));
  }

  /**
   * 新增厂牌挂失
   * @param staffNo 厂牌挂失
   * @return Result
   */
  @SysLog("新增厂牌挂失")
  @GetMapping("/save")
  public Result save(@RequestParam("parkId") Integer parkId, @RequestParam("staffNo") String staffNo){
    return success(smtBadgeLossService.saveBadgeLoss(parkId, staffNo));
  }

	/**
	 * 挂失记录导出
	 * @param
	 * @return
	 */
	@ApiOperation("挂失记录导出")
	@PostMapping("/excel")
	public ResponseEntity<byte[]> excel(@RequestBody(required = false) QueryLossInfoReqDTO reqDTO) {
		SmtBadgeLoss smtBadgeLoss = BeanUtils.transform(SmtBadgeLoss.class, reqDTO);
		return smtBadgeLossService.downLoadExcel(smtBadgeLoss);
	}

}
