package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.core.dto.MsgTempDTO;
import com.tce.smart.platform.core.dto.MsgTempListDTO;
import com.tce.smart.platform.core.service.SmtMsgTempService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2022/9/26 14:55
 */
@RestController
@RequestMapping("/message/temp")
public class SmtMsgTempController extends BaseController {

	@Autowired
	private SmtMsgTempService tempService;

	@ApiOperation("新增/修改消息模板")
	@PostMapping("/save")
	public Result<Boolean> save(@RequestBody MsgTempDTO dto) {
		return success(tempService.save(dto));
	}

	@ApiOperation("消息模板列表")
	@GetMapping("/list")
	public Result<List<MsgTempListDTO>> getById() {
		return success(tempService.getList(), MsgTempListDTO.class);
	}

	@ApiOperation("通过id查询消息模板")
	@GetMapping("/{id}")
	public Result<MsgTempDTO> getById(@PathVariable("id") Integer id) {
		return success(tempService.getById(id), MsgTempDTO.class);
	}
}
