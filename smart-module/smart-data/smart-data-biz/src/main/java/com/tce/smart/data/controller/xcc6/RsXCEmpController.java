package com.tce.smart.data.controller.xcc6;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.xcc6.core.service.IRsXCEmpService;
import com.tce.smart.data.api.dto.consume.req.RsEmpSaveReqDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 许昌C6控制器
 *
 * @author wuling
 * @date 2020-7-9
 */
@ConditionalOnProperty(name = "xc-c6.datasource.type",havingValue = "com.alibaba.druid.pool.DruidDataSource")
@RestController
@RequestMapping("/xc-rsemp")
public class RsXCEmpController extends BaseController {

	@Autowired
	private IRsXCEmpService rsEmpService;

	/**
	 * 保存Emp员工信息
	 *
	 * @return
	 */
	@PostMapping("/inner/saveEmp")
	@Inner
	@OpenApi("server")
	public Result<Boolean> saveEmp(@RequestBody RsEmpSaveReqDto saveReqDto) {
		return success(rsEmpService.saveEmp(saveReqDto.getEmpNo(),
				saveReqDto.getEmpName(),
				saveReqDto.getEmpSex(),saveReqDto.getDptNo(),saveReqDto.getGrpDate(),saveReqDto.getEmpIDNo())
		);
	}

	/**
	 * 离职Emp员工信息
	 *
	 * @return
	 */
	@PostMapping("/inner/leaveEmp")
	@Inner
	@OpenApi("server")
	public Result<Boolean> leaveEmp(@RequestBody RsEmpSaveReqDto reqDto) {
		return success(rsEmpService.leaveEmp(reqDto.getEmpNo()));
	}

	/**
	 * 离职转在职
	 *
	 * @return
	 */
	@PostMapping("/inner/intoEmp")
	@Inner
	@OpenApi("server")
	public Result<Boolean> intoEmp(@RequestBody RsEmpSaveReqDto reqDto) {
		return success(rsEmpService.intoEmp(reqDto.getEmpNo()));
	}

	/**
	 * 离职转在职
	 *
	 * @return
	 */
	@GetMapping("/inner/get-empPhoto/{empNo}")
	@Inner
	@OpenApi("server")
	public Result<Map<String,Object>> getEmpPhoto(@PathVariable("empNo") String empNo) {
		return success(rsEmpService.queryEmpPhoto(empNo));
	}
}
