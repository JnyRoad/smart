package com.tce.smart.platform.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.SearchPhotoRecordDTO;
import com.tce.smart.platform.core.dto.SearchSmtVisitorDTO;
import com.tce.smart.platform.service.SmtStaffPhotoUploadRecordService;


/**
 * 员工头像上传记录表
 *
 * @author 齐佩
 * @date 2019-12-25 15:25:26
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/staff/photo/upload")
public class SmtStaffPhotoUploadRecordController {

	private final SmtStaffPhotoUploadRecordService  service;

	  @SysLog("查询头像上传记录")
	  @GetMapping(value = "/page", produces = "application/json; charset=utf-8")
	  public Result getSmtRecordPage(Page page,  SearchPhotoRecordDTO searchPhotoRecordDTO) {
		  return  new Result<>(service.getSmtRecordPage(page,searchPhotoRecordDTO));
	  }

	  @SysLog("查询头像上传记录详情")
	  @GetMapping("/detail/{id}")
	  public Result getSmtRecordDetail(@PathVariable("id") Integer id) {
		  return  new Result<>(service.getSmtRecordDetail(id));
	  }


}
