package com.tce.smart.data.controller.guard;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.service.guard.IVcallCarService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 物流车预约
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@RestController
@AllArgsConstructor
@RequestMapping("/vcallcar")
public class VcallCarController extends BaseController {

  private final IVcallCarService vcallCarService;

  /**
   * 分页查询
   * @param page 分页对象
   * @return
   */
  /**
   * 物流车辆预约记录含驾驶员和联系方式，仅允许受控调度服务读取。
   */
  @Inner
  @OpenApi("server")
  @GetMapping("/internal/page")
  public Result getVcallCarPage(Page page) {
    return new Result <>(vcallCarService.getVcallCarPage(page));
  }

}
