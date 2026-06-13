package com.tce.smart.platform.controller;

import static com.tce.smart.common.core.model.Result.success;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.req.AddPaperRecordReqDTO;
import com.tce.smart.platform.api.dto.req.SearchPaperRecordReqDTO;
import com.tce.smart.platform.core.vo.PaperStatisticsVO;
import com.tce.smart.platform.service.SmtPaperRecordService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/paper/record")
@Api(tags = "问卷调查-问卷调查记录")
public class SmtPaperRecordController {

private final SmtPaperRecordService smtPaperRecordService;


	@ApiOperation("统计问卷")
	@GetMapping("/statistics/{id}")
	public Result<PaperStatisticsVO> statistics(@PathVariable("id") Integer id) {
			return success(smtPaperRecordService.statistics(id));
	}


	@ApiOperation("导出统计问卷")
	@GetMapping("/export/{id}")
	public void export(HttpServletResponse response,@PathVariable("id") Integer id) {
		 smtPaperRecordService.export(response,id);
	}



	/**
	 * 查询员工做过的问卷详情
	 * @param smtPaperRecord
	 * @return
	 */
	@ApiOperation("app接口---查询员工的问卷的详细信息")
	@GetMapping("/getDetail")
	public Result getDetail(SearchPaperRecordReqDTO searchPaperRecordReqDTO ) {
			return success(smtPaperRecordService.getDetail(searchPaperRecordReqDTO));
	}


	/**
	 * 查询员工做过的问卷详情
	 * @param smtPaperRecord
	 * @return
	 */
	@ApiOperation("app接口--员工提交问卷答案")
	@PostMapping("/add")
	@Inner
	public Result addRecord(@RequestBody AddPaperRecordReqDTO addPaperRecordReqDTO ) {
			return success(smtPaperRecordService.addRecord(addPaperRecordReqDTO));
	}


}
