package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.MsgTemplateDTO;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 * 消息推送模板表
 *
 * @author 齐佩
 * @date 2019-04-13 13:48:18
 */
@RestController
@AllArgsConstructor
@RequestMapping("/message/template")
public class SmtMsgTemplateController {

	private final SmtMsgTemplateService service;


	/**
	 * 查询所有短信模板
	 * @return
	 */
	@GetMapping("/all")
	public Result getSmtMessageTemplateService() {
		return service.getSmtMessageTemplate();
	}

	/**
	 * 通过id查询消息模板
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return new Result<>(service.getById(id));
	}

	/**
	 * 通过模板code查询模板
	 * @param code
	 * @return
	 */
	@GetMapping("getByCode/{code}")
	public Result getByCode(@PathVariable("code") String code) {
		return service.getByCode(code);
	}


	@GetMapping("/eamil/{id}")
	public Result getEmailById(@PathVariable("id") Integer id) {
		return service.getEmailById(id);
	}

	/**
	 * 查询所有短信模板
	 * @return
	 */
	@GetMapping("/eamil/all")
	public Result getSmtEmainTemplateService() {
		return service.getSmtEmainTemplateService();
	}

	/**
	 * 根据id修改模板
	 * @param msgTemplateDTO
	 * @return
	 */
	@PostMapping("update")
	public Result updateById(@RequestBody MsgTemplateDTO msgTemplateDTO) {
		return service.update(msgTemplateDTO);
	}

	/**
	 * 根据id修改模板接收人
	 * @param msgTemplateDTO
	 * @return
	 */
	@PostMapping("/update/receive")
	public Result updateReceive(@RequestBody MsgTemplateDTO msgTemplateDTO) {
		return service.updateReceive(msgTemplateDTO);
	}


}
