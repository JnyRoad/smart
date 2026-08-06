package com.tce.smart.data.controller.ehrview;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAttendYcxxFullRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAttendYcxxSimpleRespDTO;
import com.tce.smart.ehrview.core.entity.LvwAttendYcxx;
import com.tce.smart.ehrview.core.service.ILvwAttendYcxxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@RestController
@RequestMapping("/attend/ycxx")
public class LvwAttendYcxxController extends BaseController {
    @Autowired
    private ILvwAttendYcxxService iLvwAttendYcxxService;
	@Inner
    @OpenApi("server")
    @GetMapping("/info")
    public Result<LvwAttendYcxxSimpleRespDTO> info(@RequestParam("badge") String badge, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate){
        LvwAttendYcxx lvwAttendYcxx = iLvwAttendYcxxService.getOne(Wrappers. <LvwAttendYcxx>query().lambda()
                .eq(LvwAttendYcxx::getBadge, badge)
                .ge(LvwAttendYcxx::getAttenddate,  DateUtils.parseDate(startDate))
                .le(LvwAttendYcxx::getAttenddate,  DateUtils.parseDate(endDate))
        );
        return success(lvwAttendYcxx, LvwAttendYcxxSimpleRespDTO.class);
    }

	@Inner
	@OpenApi("server")
	@GetMapping("/infoAll")
	public Result<List<LvwAttendYcxxFullRespDTO>>  infoAll(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate){
		List<LvwAttendYcxx> list = iLvwAttendYcxxService.list(Wrappers. <LvwAttendYcxx>query().lambda()
				.ge(LvwAttendYcxx::getAttenddate,  DateUtils.parseDate(startDate))
				.le(LvwAttendYcxx::getAttenddate,  DateUtils.parseDate(endDate))
				);
		return success(list, LvwAttendYcxxFullRespDTO.class);
	}
}
