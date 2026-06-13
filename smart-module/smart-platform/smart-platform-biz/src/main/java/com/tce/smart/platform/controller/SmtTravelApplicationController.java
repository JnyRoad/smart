package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.data.api.dto.businesstrip.CcdFormtableMainDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainDt2RespDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainRespDTO;
import com.tce.smart.platform.core.dto.SearchTravelDTO;
import com.tce.smart.platform.core.model.TravelDetail;
import com.tce.smart.platform.service.SmtTravelApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 出差申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@RestController
@AllArgsConstructor
@RequestMapping("/application/travel")
public class SmtTravelApplicationController extends BaseController {

  private final  SmtTravelApplicationService smtTravelApplicationService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param searchTravelDTO 出差查询
   * @return
   */
  @SysLog("分页查询出差申请表")
  @GetMapping("/page")
  public Result<IPage<CcdFormtableMainRespDTO>> getSmtTravelApplicationPage(Page page, SearchTravelDTO searchTravelDTO) {
    return  success(smtTravelApplicationService.getSmtTravelApplicationPage(page,searchTravelDTO));
  }


  /**
   * 通过id查询出差详情
   * @param id id
   * @return Result
   */
  @SysLog("通过id查询出差详情")
  @GetMapping("/detail/{id}")
  public Result getById(@PathVariable("id") Integer id){
	  CcdFormtableMainDTO ccdFormtableMainDTO = smtTravelApplicationService.getTravelApplicationById(id);
	  return success(ccdFormtableMainDTO,TravelDetail.class);
  }

 /**
   * 通过id查询出差日程
   * @param id id
   * @return Result
   */
  @SysLog("通过id查询出差日程")
  @GetMapping("/infoDay/{id}")
  public Result getInfoDay(@PathVariable("id") Integer id){
	  return new Result<>(smtTravelApplicationService.getInfoDay(id));
  }

  /**
   * 通过id查询出差报告
   * @param id id
   * @return Result
   */
  @SysLog("通过id查询出差报告")
  @GetMapping("/infoReport/{id}")
  public Result<List<CcdFormtableMainDt2RespDTO>> getInfoReport(@PathVariable("id") Integer id){
	  return success(smtTravelApplicationService.getInfoReport(id).getData());
  }
  /**
   * 通过id查询出差流程
   * @param id id
   * @return Result
   */
  @SysLog("通过id查询出差流程")
  @GetMapping("/infoFlow/{id}")
  public Result getInfoFlow(@PathVariable("id") Integer id){
	  return new Result<>(smtTravelApplicationService.getInfoFlow(id));
  }

}
