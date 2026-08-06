package com.tce.smart.platform.controller.manage;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.manage.EditEhrSetUpReqDTO;
import com.tce.smart.platform.api.dto.req.manage.QueryEhrSetUpReqDTO;
import com.tce.smart.platform.api.dto.resp.manage.EhrSetUpRespDTO;
import com.tce.smart.platform.core.entity.manage.SmtEhrSetUp;
import com.tce.smart.platform.service.manage.SmtEhrSetUpService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import java.util.List;
import java.util.Objects;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-27 10:45:36
 */
@RestController
@AllArgsConstructor
@RequestMapping("/ehr/setup")
public class SmtEhrSetUpController extends BaseController {

  private final SmtEhrSetUpService smtEhrSetUpService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param dto
   * @return
   */
  @PostMapping("/page")
  public Result getSmtEhrSetUpPage(Page page, @RequestBody QueryEhrSetUpReqDTO dto) {
	  List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
    return success(smtEhrSetUpService.page(page,Wrappers.<SmtEhrSetUp>query().lambda()
			.eq(Objects.nonNull(dto.getParkId()),SmtEhrSetUp::getParkId, dto.getParkId())
			.eq(Objects.nonNull(dto.getSetType()),SmtEhrSetUp::getSetType, dto.getSetType())
			.in(CollectionUtils.isNotEmpty(parkIds), SmtEhrSetUp::getParkId, parkIds)), EhrSetUpRespDTO.class);
  }


  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtEhrSetUpService.getById(id));
  }

  /**
   * 新增
   * @param editEhrSetUpReqDTO
   * @return Result
   */
  @SysLog("新增")
  @PostMapping("/edit")
  public Result edit(@RequestBody EditEhrSetUpReqDTO editEhrSetUpReqDTO){
    return success(smtEhrSetUpService.edit(editEhrSetUpReqDTO));
  }

	@SysLog("发送信息")
	@GetMapping("/smg")
	public void smg(){
		smtEhrSetUpService.sendMessage();
	}


	@SysLog("定时自动确认")
	@GetMapping("/auto/confirm")
	public void autoSignTask(){
		smtEhrSetUpService.autoSignTask();
	}
}
