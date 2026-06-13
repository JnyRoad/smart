package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.RepairsApproveListReqDTO;
import com.tce.smart.platform.api.dto.req.approval.ApproveListQueryDTO;
import com.tce.smart.platform.api.dto.resp.RepairsApprovalListRespDTO;
import com.tce.smart.platform.core.entity.ApproveList;
import com.tce.smart.platform.core.vo.ApproveListVO;
import com.tce.smart.platform.service.ApproveListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * 待审批信息
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-列表审批")
@RequestMapping("/approve/list")
public class ApproveListController extends BaseController {

	private final ApproveListService approveListService;

	/**
	 * 分页查询
	 *
	 * @param page        分页对象
	 * @param approveList 待审批信息
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtAlarmPage(Page page, ApproveList approveList) {
		IPage pageList = approveListService.getApproveList(page, approveList);
		return success(pageList, ApproveListVO.class);
	}

	@ApiOperation(value = "园区报修待审批")
	@GetMapping("/repairs/list")
	public Result getRepairsPage(Page page, RepairsApproveListReqDTO reqDTO) {
		IPage pageList = approveListService.getRepairsApproveList(page, reqDTO);
		return success(pageList, RepairsApprovalListRespDTO.class);
	}

	@GetMapping("/new/page")
	@ApiOperation(value = "公众号分页查询待审批列表")
	public Result<IPage<ApproveListVO>> getApprovePage(Page page, @Valid ApproveListQueryDTO queryDTO) {
		return success(approveListService.getNewPage(page, queryDTO), ApproveListVO.class);
	}

	/**
	 * 通过id查询警报信息记录
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return new Result<>(approveListService.getById(id));
	}

	/**
	 * 新增待审批信息记录
	 *
	 * @param approveList 待审批信息
	 * @return Result
	 */
	@SysLog("新增待审批信息记录")
	@PostMapping("/save")
	public Result save(@RequestBody ApproveList approveList) {
		return new Result<>(approveListService.saveApproveList(approveList));
	}

	/**
	 * 修改警报信息记录
	 *
	 * @param approveList 待审批信息
	 * @return Result
	 */
	@SysLog("修改待审批信息记录")
	@PostMapping("/update")
	public Result updateById(@RequestBody ApproveList approveList) {
		return new Result<>(approveListService.updateState(approveList));
	}

	/**
	 * 通过id删除待审批信息记录
	 *
	 * @param id id
	 * @return Result
	 */
	@SysLog("删除待审批信息记录")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Integer id) {
		return new Result<>(approveListService.removeById(id));
	}


}
