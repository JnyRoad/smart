package com.tce.smart.data.api.feign.consume;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.consume.req.RsEmpSaveReqDto;
import com.tce.smart.data.api.dto.consume.resp.RsEmpRespDTO;
import com.tce.smart.data.api.dto.consume.resp.WorkTimeDetailDTO;
import com.tce.smart.data.api.dto.consume.resp.WorkTimeRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 人员Emp信息
 *
 * @author fushiping
 * @date 2020-7-9
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteRsEmpService {

	/**
	 * 根据员工工号获得emp信息
	 *
	 * @param empNo 员工工号
	 * @return ture-成功,false-失败
	 */
	@GetMapping("/rsemp/inner/getEmp")
	Result<RsEmpRespDTO> getRsEmp(@RequestParam("empNo") String empNo,
										 @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据工号和时间查询考勤工时明细
	 *
	 * @param empNo emp系统员工id
	 * @return
	 */
	@GetMapping("/rsemp/inner/getEmpWorkDetail")
	Result<List<WorkTimeDetailDTO>> getEmpWorkDetail(@RequestParam("empNo") String empNo, @RequestParam("date") Date date,
													 @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询正班天数
	 *
	 * @return
	 */
	@GetMapping("/rsemp/inner/getNormalWorkDays")
	Result<Double> getNormalWorkDays(@RequestParam("empNo") String empNo,
											@RequestParam("startDate") Date startDate,
											@RequestParam("endDate") Date endDate,
									 @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询当月节假日
	 *
	 * @return
	 */
	@GetMapping("/rsemp/inner/getFreeDays")
	Result<WorkTimeRespDTO> getFreeDays(@RequestParam("empNo") String empNo, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 保存Emp员工信息
	 *
	 * @return
	 */
	@PostMapping("/rsemp/inner/saveEmp")
	Result<Boolean> saveEmp(@RequestBody RsEmpSaveReqDto saveReqDto,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 离职Emp员工信息
	 *
	 * @return
	 */
	@PostMapping("/rsemp/inner/leaveEmp")
	Result<Boolean> leaveEmp(@RequestBody RsEmpSaveReqDto saveReqDto,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 离职转在职
	 *
	 * @return
	 */
	@PostMapping("/rsemp/inner/intoEmp")
	Result<Boolean> intoEmp(@RequestBody RsEmpSaveReqDto saveReqDto,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据员工号获取卡片
	 * @param empNo
	 * @param from
	 * @return
	 */
	@GetMapping("/rsemp/inner/getEmpCard")
	Result<String> getEmpCard(@RequestParam("empNo") String empNo, @RequestHeader(SecurityConstants.FROM) String from);

}
