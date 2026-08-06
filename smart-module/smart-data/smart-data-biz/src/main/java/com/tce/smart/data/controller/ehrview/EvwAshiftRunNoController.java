package com.tce.smart.data.controller.ehrview;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.EvwAshiftRunNoDTO;
import com.tce.smart.ehrview.core.entity.EvwAshiftRunNo;
import com.tce.smart.ehrview.core.service.IEvwAshiftRunNoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liangyuan
 * @since 2019-05-03
 */
@RestController
@RequestMapping("/evwAshiftRunNo")
public class EvwAshiftRunNoController extends BaseController {
    @Autowired
    private IEvwAshiftRunNoService ivwAshiftRunNoService;

    @Inner
    @GetMapping("/info")
    public Result<EvwAshiftRunNoDTO> info(@RequestParam("badge") String badge, @RequestParam("empRunDate") String empRunDate){
	EvwAshiftRunNo evwAshiftRunNo = ivwAshiftRunNoService.getOne(Wrappers. <EvwAshiftRunNo>query().lambda()
                .eq(EvwAshiftRunNo::getEmpNo, badge)
                .eq(EvwAshiftRunNo::getEmpRunDate,  DateUtils.parseDate(empRunDate))
        );
		EvwAshiftRunNoDTO evwAshiftRunNoDTO = new EvwAshiftRunNoDTO();
		BeanUtils.copyProperties(evwAshiftRunNo,evwAshiftRunNoDTO);

        return success(evwAshiftRunNoDTO);
    }
}
