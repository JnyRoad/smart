package com.tce.smart.platform.controller;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.SmtAlarmRecordDTO;
import com.tce.smart.platform.core.dto.AlarmRecordDTO;
import com.tce.smart.platform.core.entity.SmtAlarmRecord;
import com.tce.smart.platform.core.vo.AlarmRecordVO;
import com.tce.smart.platform.service.SmtAlarmRecordService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 警报记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:38
 */
@RestController
@AllArgsConstructor
@RequestMapping("/alarm/record")
public class SmtAlarmRecordController extends BaseController {

  private final  SmtAlarmRecordService smtAlarmRecordService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param alarmRecordDTO 警报记录表
   * @param alarmTime 查询时间数组
   * @return
   */
  @GetMapping("/page")
  public Result getSmtAlarmRecordPage(Page page, AlarmRecordDTO alarmRecordDTO,@RequestParam(value = "alarmTime[]",required=false) String[] alarmTime,@RequestParam(value = "areaIdArray[]",required=false) Integer[] areaIdArray) {
	  if(ArrayUtil.isNotEmpty(areaIdArray)) {
		  if(areaIdArray.length > 0) {
			  alarmRecordDTO.setParkId(areaIdArray[0]);
		  }
		  if(areaIdArray.length > 1) {
			  alarmRecordDTO.setPid(areaIdArray[1]);
		  }
		  if(areaIdArray.length > 2) {
			  alarmRecordDTO.setAreaId(areaIdArray[2]);
		  }
	  }
	  List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
	  alarmRecordDTO.setParkIds(parkIds);
	  smtAlarmRecordService.getAlarmRecord(page,alarmRecordDTO,alarmTime);
	  return success(page, AlarmRecordVO.class);
  }


  /**
   * 通过id查询警报记录表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result <>(smtAlarmRecordService.getById(id));
  }

  /**
   * 新增警报记录表
   * @param smtAlarmRecordDTO 警报记录表
   * @return Result
   */
  @SysLog("新增警报记录表")
  @PostMapping("/save")
  public Result<Boolean> save(@RequestBody SmtAlarmRecordDTO smtAlarmRecordDTO){
	  SmtAlarmRecord  smtAlarmRecord = new SmtAlarmRecord();
	  BeanUtils.copyProperties(smtAlarmRecordDTO,smtAlarmRecord);
    return new Result <>(smtAlarmRecordService.saveSmtAlarmRecord(smtAlarmRecord));
  }

  /**
   * 修改警报记录表
   * @param smtAlarmRecord 警报记录表
   * @return Result
   */
  @SysLog("修改警报记录表")
  @PostMapping
  public Result updateById(@RequestBody SmtAlarmRecord smtAlarmRecord){
    return new Result <>(smtAlarmRecordService.updateById(smtAlarmRecord));
  }

  /**
   * 通过id删除警报记录表
   * @param id id
   * @return Result
   */
  @SysLog("删除警报记录表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result <>(smtAlarmRecordService.removeById(id));
  }
}
