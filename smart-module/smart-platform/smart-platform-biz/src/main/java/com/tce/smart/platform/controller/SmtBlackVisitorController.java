package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.visitormanage.BlackVisitorAddReqDTO;
import com.tce.smart.platform.core.entity.SmtBlackVisitor;
import com.tce.smart.platform.core.vo.BlackVisitorVO;
import com.tce.smart.platform.service.SmtBlackVisitorService;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 黑名单
 * @author QIPEI
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/black/visitor")
public class SmtBlackVisitorController extends BaseController {

	private final SmtBlackVisitorService smtBlackVisitorService;

	/**
	 * 分页查询访客黑名单
	 * @param page
	 * @param smtBlackVisitor
	 * @return
	 */
	@GetMapping("/page")
	public Result<IPage<BlackVisitorVO>> getVisitorPage(Page page, SmtBlackVisitor smtBlackVisitor) {
		return new Result<>(smtBlackVisitorService.page(page,smtBlackVisitor));
	}


	@GetMapping("/hr/page")
	public Result getHrBlackPage(Page page, SmtBlackVisitor smtBlackVisitor) {
		return smtBlackVisitorService.getHrBlackPage(page,smtBlackVisitor.getCardNo(),smtBlackVisitor.getPersonName());
	}

	/**
	 * 移除黑名单访客
	 * @param id
	 * @return
	 */
	@SysLog("删除黑名单访客")
	@GetMapping("/delete/{id}")
	public Result<Boolean> deleteById(@PathVariable Integer id) {
		return smtBlackVisitorService.removeVisitorById(id);
	}


	@SysLog("新增黑名单访客")
	@PostMapping("/add")
	public Result<Boolean> save(@RequestBody @Valid BlackVisitorAddReqDTO smtBlackVisitor) {
		return smtBlackVisitorService.addVisitor(smtBlackVisitor);
	}

	@ApiOperation("黑名单访客批量导入")
	@PostMapping("/import/{parkId}")
	public Result<List<BlackVisitorAddReqDTO>> batchImport(@RequestBody @Valid List<BlackVisitorAddReqDTO> reqDTO, @PathVariable("parkId") Integer parkId) {
		return success(smtBlackVisitorService.batchImport(reqDTO, parkId));
	}

}
