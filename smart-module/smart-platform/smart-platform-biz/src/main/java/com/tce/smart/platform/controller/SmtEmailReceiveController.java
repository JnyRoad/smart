package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtEmailReceive;
import com.tce.smart.platform.core.service.SmtEmailReceiveService;
import lombok.AllArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * 准备邮件接收者
 *
 * @author 齐佩
 * @date 2019-04-13 13:48:18
 */
@RestController
@AllArgsConstructor
@RequestMapping("/email/receive")
public class SmtEmailReceiveController {
	private final SmtEmailReceiveService service;

	/**
	 * 通过id删除邮件接收者
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@Delete("/{id}")
	public Result removeById(@PathVariable("id") Integer id) {
		return new Result<>(service.removeById(id));
	}


	@GetMapping("/get/{templateId}")
	public Result getById(@PathVariable("templateId") Integer templateId) {
		return service.getEmailById(templateId);
	}

	/**
	 * 修改接收者
	 * @param email
	 * @return
	 */
	@PostMapping("/update")
	public Result updateById(@RequestBody SmtEmailReceive email) {
		return service.updateId(email);
	}


	/**
	 * 添加接收者
	 * @param email
	 * @return
	 */
	@PostMapping("/add")
	public Result add(@RequestBody SmtEmailReceive email) {
		return service.add(email);
	}



}
