package com.tce.smart.algorithm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.algorithm.api.dto.req.UpdateAlgorithmConfigDTO;
import com.tce.smart.algorithm.api.dto.resp.AlgorithmConfigDetailDTO;
import com.tce.smart.algorithm.api.dto.resp.AlgorithmConfigListDTO;
import com.tce.smart.algorithm.service.AlgorithmConfigService;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.algorithm.entity.AlgorithmConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author wuxinjian
 * @date 2020-02-06 14:58:10
 */
@RestController
@AllArgsConstructor
@RequestMapping("/config")
@Api(value = "config", tags = "算法配置")
public class AlgorithmConfigController extends BaseController {

	private final AlgorithmConfigService algorithmConfigService;

	/**
	 * 获取所有算法
	 *
	 * @return
	 */
	@ApiOperation("获取算法列表")
	@PreAuthorize("@pms.hasPermission('algorithm_config_manage')")
	@GetMapping("/algorithms")
	public Result<List<AlgorithmConfigListDTO>> algorithms(@RequestParam(value = "type", required = false) String type) {
		List<AlgorithmConfig> iPage = algorithmConfigService.getList(type);
		return success(iPage, AlgorithmConfigListDTO.class);
	}

	/**
	 * 分页查询
	 *
	 * @param page          分页对象
	 * @param algorithmType
	 * @return
	 */
	@ApiOperation("获取算法分页列表")
	@PreAuthorize("@pms.hasPermission('algorithm_config_manage')")
	@GetMapping("/page")
	public Result<IPage<AlgorithmConfigListDTO>> getAlgorithmConfigPage(Page<AlgorithmConfig> page, @RequestParam(value = "algorithmType", required = false) String algorithmType) {
		IPage<AlgorithmConfig> iPage = algorithmConfigService.getPageList(page, algorithmType);
		return success(iPage, AlgorithmConfigListDTO.class);
	}


	/**
	 * 通过id查询
	 *
	 * @param algorithmType
	 * @return Result
	 */
	@ApiOperation("获取算法详情")
	@PreAuthorize("@pms.hasPermission('algorithm_config_manage')")
	@GetMapping("/{algorithmType}")
	public Result<AlgorithmConfigDetailDTO> getById(@PathVariable("algorithmType") String algorithmType) {
		AlgorithmConfig algorithmConfig = algorithmConfigService.getByAlgorithmType(algorithmType);
		return success(algorithmConfig, AlgorithmConfigDetailDTO.class);
	}

	/**
	 * 修改
	 *
	 * @param updateAlgorithmConfigDTO
	 * @return Result
	 */
	@SysLog("修改算法配置")
	@ApiOperation("修改算法配置")
	@PreAuthorize("@pms.hasPermission('algorithm_config_manage')")
	@PostMapping
	public Result<Boolean> updateById(@RequestBody @Valid UpdateAlgorithmConfigDTO updateAlgorithmConfigDTO) {
		algorithmConfigService.updateConfig(updateAlgorithmConfigDTO);
		return success(Boolean.TRUE);
	}
}
