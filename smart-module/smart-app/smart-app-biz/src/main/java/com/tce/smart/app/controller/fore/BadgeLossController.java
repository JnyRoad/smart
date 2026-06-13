package com.tce.smart.app.controller.fore;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.service.fore.BadgeLossService;
import com.tce.smart.app.vo.fore.BadgeInfoVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.badge.QueryLossInfoReqDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeLossInfoRespDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Api(tags = "厂牌挂失")
@RequestMapping("/badge/loss")
public class BadgeLossController extends BaseController {

	private final BadgeLossService badgeLossService;


	/**
	 * 获得厂牌与员工信息
	 * @return
	 */
	@GetMapping("/info")
	@ApiOperation("获得厂牌与员工信息")
	public Result<BadgeInfoVo> getBadgeInfo() {
		return success(badgeLossService.getBadgeInfo());
	}

	/**
	 * 厂牌挂失
	 * @param cardId
	 * @return
	 */
	@GetMapping("/lock")
	@ApiOperation("厂牌挂失")
	public Result<Boolean> badgeLoss(@RequestParam("cardId") Long cardId, @RequestParam("parkId") Integer parkId) {
		return success(badgeLossService.badgeLoss(cardId, parkId));
	}


}
