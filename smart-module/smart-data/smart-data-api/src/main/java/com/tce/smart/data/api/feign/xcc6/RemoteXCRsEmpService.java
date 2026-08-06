package com.tce.smart.data.api.feign.xcc6;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.consume.req.RsEmpSaveReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 人员Emp信息
 *
 * @author wuling
 * @date 2020-7-9
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteXCRsEmpService {

	/**
	 * 保存Emp员工信息
	 *
	 * @return
	 */
	@PostMapping("/xc-rsemp/inner/saveEmp")
	Result<Boolean> saveEmp(@RequestBody RsEmpSaveReqDto saveReqDto,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 离职Emp员工信息
	 *
	 * @return
	 */
	@PostMapping("/xc-rsemp/inner/leaveEmp")
	Result<Boolean> leaveEmp(@RequestBody RsEmpSaveReqDto saveReqDto,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 离职Emp员工信息
	 *
	 * @return
	 */
	@PostMapping("/xc-rsemp/inner/intoEmp")
	Result<Boolean> intoEmp(@RequestBody RsEmpSaveReqDto saveReqDto,@RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/xc-rsemp/inner/get-empPhoto/{empNo}")
	Result<Map<String,Object>> getEmpPhoto(@PathVariable("empNo") String empNo, @RequestHeader(SecurityConstants.FROM) String from);
}
