package com.tce.smart.platform.controller.securityzone;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthRelationRespDTO;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *保密区权限关联
 * @author fushiping
 * @date 2021-07-29 11:12:53
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-保密区权限关联")
@RequestMapping("/security/auth/relation")
public class SmtSecurityAuthRelationController extends BaseController {

  private final SmtSecurityAuthRelationService smtSecurityAuthRelationService;

	/**
	 * 查询
	 * @return
	 */
	@ApiOperation("根据保密区ID查询权限-保密区项目维护")
	@GetMapping("/list/{securityZoneId}")
	public Result getAuthList(@PathVariable Long securityZoneId ) {
		return success(smtSecurityAuthRelationService.getList(securityZoneId), SecurityAuthRelationRespDTO.class);
	}

}
