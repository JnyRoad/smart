package com.tce.smart.data.controller.ehrview;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAyearholidayRespDTO;
import com.tce.smart.ehrview.core.entity.LvwAyearholiday;
import com.tce.smart.ehrview.core.service.ILvwAyearholidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@RestController
@RequestMapping("/year/holiday")
public class LvwAyearholidayController extends BaseController {
    @Autowired
    private ILvwAyearholidayService iLvwAyearholidayService;
    /**
     * 根据员工号获取剩余年假天数
     * @param badge
     * @return
     */
    @Inner
    @OpenApi("server")
    @GetMapping("/info")
    public Result<LvwAyearholidayRespDTO> info(@RequestParam("badge") String badge) {
        LvwAyearholiday lvwAyearholiday = iLvwAyearholidayService.getByBadge(badge);
        return success(lvwAyearholiday, LvwAyearholidayRespDTO.class);
    }
}
