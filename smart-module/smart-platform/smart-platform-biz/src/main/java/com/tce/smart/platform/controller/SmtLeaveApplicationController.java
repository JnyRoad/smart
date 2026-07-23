package com.tce.smart.platform.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.consume.resp.WorkTimeDetailDTO;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.platform.api.dto.req.LeaveHandoverSubmitReqDTO;
import com.tce.smart.platform.api.dto.resp.WorkDetailDTO;
import com.tce.smart.platform.core.dto.LeaveApplicationDTO;
import com.tce.smart.platform.core.dto.LeaveApplicationRecordDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.model.LeaveReason;
import com.tce.smart.platform.core.model.LeaveRecordList;
import com.tce.smart.platform.core.model.LeaveType;
import com.tce.smart.platform.core.model.ProcessRecordFlow;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.core.vo.LeaveApplicationRecordDetailVO;
import com.tce.smart.platform.core.vo.LeaveApplicationRecordVO;
import com.tce.smart.platform.core.vo.LeaveApplicationVO;
import com.tce.smart.platform.core.vo.LeaveRecordVO;
import com.tce.smart.platform.service.ILeaveApplicationService;
import com.tce.smart.tool.util.ToolUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 离职申请表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
@Api(tags = "离职管理")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/leave/application")
public class SmtLeaveApplicationController extends BaseController{

	private final SmtLeaveApplicationService smtLeaveApplicationService;

	private final ILeaveApplicationService leaveApplicationService;

	private final RemoteRsEmpService remoteRsEmpService;


	/**
	 * 获取离职记录 分页查询
	 * @param page 分页对象
	 * @param leaveApplicationRecordDTO 离职查询信息
	 */
	@SysLog("获取离职记录")
	@GetMapping("/page")
	public Result page(Page page, LeaveApplicationRecordDTO leaveApplicationRecordDTO){
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		leaveApplicationRecordDTO.setParkIds(parkIds);
		IPage<SmtLeaveApplication> list = smtLeaveApplicationService.getPage(page,leaveApplicationRecordDTO);
		return success(list, LeaveApplicationRecordVO.class);
	}

	/**
	 * 获取离职记录详情
	 */
	@SysLog("获取离职记录")
	@GetMapping("/detail/{id}")
	public Result page(@PathVariable("id") Integer id){
		SmtLeaveApplication smtLeaveApplication = smtLeaveApplicationService.getById(id);
		return success(smtLeaveApplication, LeaveApplicationRecordDetailVO.class);
	}


	/**
	 * 通过员工号查询离职申请表
	 *
	 * @param badge
	 *            员工号
	 * @return Result
	 */
	@GetMapping("/{badge}")
	public Result getByBadge(@PathVariable("badge") String badge) {
	    SmtLeaveApplication leaveApplication = smtLeaveApplicationService.getLeaveApplicationRecord(badge);
		return success(leaveApplication, LeaveApplicationVO.class);
	}

	/**
	 * 新增离职申请表
	 *
	 * @param leaveApplicationDTO
	 *            离职申请表
	 * @return Result
	 */
	@SysLog("新增离职申请表")
	@PostMapping("/save")
	public Result save(@RequestBody LeaveApplicationDTO leaveApplicationDTO) {
		return leaveApplicationService.saveLeaveApplication(leaveApplicationDTO);
	}

	/**
	 * 离职类型
	 *
	 * @return
	 */
	@SysLog("获取离职类型")
	@GetMapping("/type")
	public Result getLeaveType(){
	    List<SysDict> list = leaveApplicationService.getLeaveType();
		return success(list, LeaveType.class);
	}

	/**
	 * 离职原因
	 *
	 * @return
	 */
	@SysLog("获取离职原因")
	@GetMapping("/reason")
	public Result getLeaveReason(){
	    List<SysDict> list = leaveApplicationService.getLeaveReason();
		return success(list, LeaveReason.class);
	}

	/**
	 * 获取剩余年假天数
	 * @param badge 员工号
	 * @return
	 */
	@SysLog("获取剩余年假天数")
	@GetMapping("/year/holiday/{badge}")
	public Result getYearHoliday(@PathVariable("badge") String badge) {
		return new Result<>(leaveApplicationService.getYearHoliday(badge));
	}



	/**
     * 获取离职记录 分页查询
     * @param page 分页对象
     * @param leaveApplicationDTO 离职查询信息
     */
    @SysLog("获取离职记录")
    @GetMapping("/record/page")
    public Result getProcessRecord(Page page,LeaveApplicationDTO leaveApplicationDTO){
        IPage<LeaveRecordVO> list = smtLeaveApplicationService.getProcessRecord(page,leaveApplicationDTO.getBadge(),leaveApplicationDTO.getLeaveStatus());
        return success(list, LeaveRecordList.class);
    }

	/**
	 * 获取离职审批记录详情
	 * @param processId 流程编号
	 */
	@SysLog("获取离职审批记录详情")
	@GetMapping("/record/detail/{processId}")
	public Result getLeaveApplicationRecord(@PathVariable("processId") String processId){
	    List<SmtProcessRecord> list = smtLeaveApplicationService.getLeaveApplication(processId);
        return success(list, ProcessRecordFlow.class);
	}

	/**
	 *  同步OA流程方法
	 */
	@SysLog(" 同步OA流程方法")
	@Inner
	@OpenApi("server")
	@GetMapping("/sysn/record")
	public void sysnProcessRecord(){
		leaveApplicationService.sysnProcessRecord();
	}

	@SysLog("获取离职记录详情")
	@GetMapping("/fail/{processId}")
	public Result failL(@PathVariable("processId") String processId) {
		return new Result<>(leaveApplicationService.failLeaveApplication(processId));
	}

	/**
	 * 查询考勤工时明细
	 * @return
	 */
	@ApiOperation("查询考勤工时明细")
	@GetMapping("/workDetail")
	public Result<WorkDetailDTO> getWorkDetail() {
		return new Result<>(leaveApplicationService.getWorkDetail());
	}

	/**
	 * 开始离职工作交接
	 * @return
	 */
	@ApiOperation("开始离职工作交接")
	@PostMapping("/workConnect")
	public Result<Boolean> setWorkConnect() {
		return new Result<>(leaveApplicationService.setWorkConnect());
	}

	/**
	 * 查询审批人待处理的离职交接项
	 * @return
	 */
	@ApiOperation("查询审批人待处理的离职交接项")
	@GetMapping("/approveitem/{id}")
	public Result<LeaveApplicationRecordDetailVO> getApproveItem(@ApiParam(name = "id",value = "离职申请Id",required = true) @PathVariable Integer id) {
		return new Result<>(leaveApplicationService.getApproveItem(id));
	}


	/**
	 * 审批人提交离职交接项
	 * @return
	 */
	@ApiOperation("审批人提交离职交接项")
	@PostMapping("/submit/item")
	public Result<Boolean> submitItem(@RequestBody LeaveHandoverSubmitReqDTO submitReqDTO) {
		return new Result<>(leaveApplicationService.submitItem(submitReqDTO));
	}
}
