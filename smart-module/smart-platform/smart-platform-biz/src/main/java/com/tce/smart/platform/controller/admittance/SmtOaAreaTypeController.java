package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAuthTypeRespDTO;
import com.tce.smart.platform.service.admittance.SmtOaAreaTypeService;
import com.tce.smart.tool.enums.OaSelectItemTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author fushiping
 * @date 2021-08-17 17:45:30
 */
@Api(tags = "platform-OA字典获取")
@RestController
@AllArgsConstructor
@RequestMapping("/admittance/area/type")
public class SmtOaAreaTypeController extends BaseController {

  private final SmtOaAreaTypeService smtOaAreaTypeService;


	/**
	 * 同步OA区域类型
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/sync/task")
	@ApiOperation("同步OA区域类型")
	public Result<Boolean> syncOaArea() {
		return success(smtOaAreaTypeService.syncOaTask());
	}

	/**
	 * 同步OA区域类型
	 * @return
	 */
	@GetMapping("/sync/{type}")
	@ApiOperation("手动同步OA区域类型")
	public Result<Boolean> sync(@PathVariable("type") Integer type) {
		return success(smtOaAreaTypeService.syncArea(type));
	}

	/**
	 * 获得所有区域类型
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperation("获得所有区域类型")
	public Result getAllType(@RequestParam("type") Integer type) {
		return success(smtOaAreaTypeService.getAreaType(type), AdmittanceAuthTypeRespDTO.class);
	}

	/**
	 * 获得所有区域类型
	 * @return
	 */
	@GetMapping("/admittance/factory/list")
	@ApiOperation("获得入场申请工厂类型")
	public Result getAdmittanceFactoryType() {
		return success(smtOaAreaTypeService.getAreaType(OaSelectItemTypeEnum.ADMITTANCE_FACTORY_TYPE.getCode()), AdmittanceAuthTypeRespDTO.class);
	}

	/**
	 * 获得所有区域类型
	 * @return
	 */
	@GetMapping("/security/factory/list")
	@ApiOperation("获得入场申请工厂类型")
	public Result getSecurityFactoryType() {
		return success(smtOaAreaTypeService.getAreaType(OaSelectItemTypeEnum.SECURITY_FACTORY_TYPE.getCode()), AdmittanceAuthTypeRespDTO.class);
	}

}
