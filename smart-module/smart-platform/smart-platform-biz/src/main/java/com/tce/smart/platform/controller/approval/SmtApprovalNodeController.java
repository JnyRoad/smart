package com.tce.smart.platform.controller.approval;

import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.approval.EditApprovalNodeReqDTO;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalNodeRespDTO;
import com.tce.smart.platform.core.entity.SmtApproval;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.platform.core.service.SmtApprovalService;
import com.tce.smart.platform.service.approval.ApprovalNodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import java.util.List;
import java.util.Objects;

/**
 * @author fushiping
 * @date 2021-04-08 16:25:18
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-审批节点")
@RequestMapping("/approval/node")
public class SmtApprovalNodeController extends BaseController {

	private final SmtApprovalNodeService smtApprovalNodeService;

	private final ApprovalNodeService approvalNodeService;

	private final SmtApprovalService approvalService;

	/**
	 * 查询
	 *
	 * @param approvalId
	 * @return
	 */
	@GetMapping("/list/{approvalId}")
	@ApiOperation("查询")
	public Result<List<ApprovalNodeRespDTO>> getSmtApprovalNodeList(@PathVariable("approvalId") Integer approvalId) {
		return success(smtApprovalNodeService.getList(approvalId), ApprovalNodeRespDTO.class);
	}

	/**
	 * 新增
	 * @param approvalId
	 * @param isUploadImg
	 * @param editApprovalNode
	 * @return Result
	 */
	@SysLog("新增")
	@PostMapping("/save")
	@ApiOperation("新增")
	public Result save(@RequestParam(value = "approvalId", required = false) Integer approvalId,
					   @RequestParam(value = "isUploadImg", required = false) Integer isUploadImg,
					   @RequestBody List<EditApprovalNodeReqDTO> editApprovalNode) {
		if(Objects.nonNull(approvalId)) {
			SmtApproval approval = approvalService.getById(approvalId);
			if (Objects.isNull(approval)) {
				throw new SmartException("该园区审批配置不存在");
			}
			approval.setIsUploadImg(isUploadImg);
			approvalService.updateById(approval);
		}
		return success(approvalNodeService.saveNode(editApprovalNode));
	}

	/**
	 * 修改
	 * @param approvalId
	 * @param isUploadImg
	 * @param editApprovalNode
	 * @return Result
	 */
	@SysLog("修改")
	@PostMapping("/update")
	@ApiOperation("修改")
	public Result updateById(@RequestParam(value = "approvalId",required = false) Integer approvalId,
							 @RequestParam(value = "isUploadImg",required = false) Integer isUploadImg,
							 @RequestBody List<EditApprovalNodeReqDTO> editApprovalNode) {
		if(Objects.nonNull(approvalId)) {
			SmtApproval approval = approvalService.getById(approvalId);
			if (Objects.isNull(approval)) {
				throw new SmartException("该园区审批配置不存在");
			}
			approval.setIsUploadImg(isUploadImg);
			approvalService.updateById(approval);
		}
		return success(approvalNodeService.updateNode(editApprovalNode));
	}

}
