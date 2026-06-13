package com.tce.smart.data.controller.ehrview;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.data.api.dto.ehrview.EvwEappraisDTO;
import com.tce.smart.ehrview.core.entity.EvwEapprais;
import com.tce.smart.ehrview.core.service.IEvwEappraisService;
import org.springframework.beans.BeanUtils;
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
@RequestMapping("/eapprais")
public class EvwEappraisController extends BaseController {
    @Autowired
    private IEvwEappraisService iEvwEappraisService;

    /**
     * 根据员工号获取评优人员信息
     * @param badge
     * @return
     */
//    @Inner
    @GetMapping("/info")
    public Result info(@RequestParam("badge") String badge){
        List<EvwEapprais> evwEapprais = iEvwEappraisService.getListByBadge(badge);

		EvwEappraisDTO evwEappraisDTO = new EvwEappraisDTO();
		BeanUtils.copyProperties(evwEapprais,evwEappraisDTO);

        return success(evwEappraisDTO);
    }
}
