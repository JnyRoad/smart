package com.tce.smart.data.controller.ehrview;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.LvwAcardlostDTO;
import com.tce.smart.ehrview.core.entity.LvwAcardlost;
import com.tce.smart.ehrview.core.service.ILvwAcardlostService;
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
 * @author WangJinbo123
 * @since 2019-05-03
 */
@RestController
@RequestMapping("/lvw/acardlost")
public class LvwAcardlostController extends BaseController {
    @Autowired
    private ILvwAcardlostService iLvwAcardlostService;

    /**
     * 根据工号和出勤日期查询  补卡信息
     * @param badge
     * @param startDate
     * @return
     */
    @Inner
    @OpenApi("server")
    @GetMapping("/info")
    public Result<LvwAcardlostDTO> getByBadge(@RequestParam("badge") String badge, @RequestParam("startDate") String startDate){
        LvwAcardlost lvwAcardlost = iLvwAcardlostService.getOne(
                Wrappers.<LvwAcardlost>query().lambda()
                        .eq(LvwAcardlost::getBadge, badge)
                        .eq(LvwAcardlost::getKqStartDate, DateUtils.parseDate(startDate))
        );
		LvwAcardlostDTO lvwAcardlostDTO = new LvwAcardlostDTO();
		BeanUtils.copyProperties(lvwAcardlost,lvwAcardlostDTO);
        return success(lvwAcardlostDTO);
    }
}
