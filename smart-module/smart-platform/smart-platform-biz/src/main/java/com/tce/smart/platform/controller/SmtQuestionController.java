package com.tce.smart.platform.controller;


import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.resp.SearchQuestionListRespDTO;
import com.tce.smart.platform.core.entity.SmtQuestion;
import com.tce.smart.platform.service.SmtQuestionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

import static com.tce.smart.common.core.model.Result.success;

@RestController
@AllArgsConstructor
@RequestMapping("/question")
@Api(tags = "问卷调查-问卷问题控制类")
public class SmtQuestionController extends BaseController {


	private final SmtQuestionService  smtQuestionService;

	@GetMapping("/list")
	@ApiOperation("查询问卷的问题列表及选项")
	public Result<List<SearchQuestionListRespDTO>> getQuestionList(SmtQuestion smtQuestion) {
		List<SmtQuestion> list = smtQuestionService.list(Wrappers.query(smtQuestion));
		return success(list,SearchQuestionListRespDTO.class);
	}



	@GetMapping("/getType")
	@ApiOperation("查询问卷的问题类型")
	public Result<List<Map<String, Object>>> getType() {
		return success(smtQuestionService.getType());
	}


}