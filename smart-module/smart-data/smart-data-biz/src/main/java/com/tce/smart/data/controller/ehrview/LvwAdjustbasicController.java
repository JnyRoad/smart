package com.tce.smart.data.controller.ehrview;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAdjustbasicFullRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAdjustbasicRespDTO;
import com.tce.smart.ehrview.core.entity.LvwAdjustbasic;
import com.tce.smart.ehrview.core.service.ILvwAdjustbasicService;
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
@RequestMapping("/adjust/basic")
public class LvwAdjustbasicController extends BaseController {
    @Autowired
    private ILvwAdjustbasicService iLvwAdjustbasicService;

    @Inner
    @GetMapping("/info")
    public Result<LvwAdjustbasicRespDTO> info(@RequestParam("badge") String badge,@RequestParam("term") String term){
        LvwAdjustbasic lvwAdjustbasic = iLvwAdjustbasicService.getOne(Wrappers.<LvwAdjustbasic>query().lambda()
		.eq(LvwAdjustbasic::getBadge, badge)
		.eq(LvwAdjustbasic::getTerm, DateUtils.parse(term))
		);
        return success(lvwAdjustbasic, LvwAdjustbasicRespDTO.class);
    }

    @Inner
    @GetMapping("/getByBadge")
    public Result<List<LvwAdjustbasicFullRespDTO>> getByBadge(@RequestParam("badge") String badge) {
/*		List<SmtSnapPerson> smtSnapList = smtSnapPersonMapper.selectList(Wrappers.<SmtSnapPerson> query().lambda()
				.eq(SmtSnapPerson::getPersonId, id)
				.orderByDesc(SmtSnapPerson::getSnapTime));*/
        List<LvwAdjustbasic> lvwAdjustbasic = iLvwAdjustbasicService.list(Wrappers.<LvwAdjustbasic>query().lambda()
		.eq(LvwAdjustbasic::getBadge, badge)
		.orderByAsc(LvwAdjustbasic::getTerm));
        return success(lvwAdjustbasic,LvwAdjustbasicFullRespDTO.class);
    }
}
