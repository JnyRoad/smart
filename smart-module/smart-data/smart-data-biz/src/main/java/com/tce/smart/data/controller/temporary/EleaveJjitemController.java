package com.tce.smart.data.controller.temporary;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.data.api.dto.temporary.req.EleaveJjitemReqDTO;
import com.tce.smart.temporary.core.entity.EleaveJjitem;
import com.tce.smart.temporary.core.service.IEleaveJjitemService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Controller
@RequestMapping("/eleaveJjitem")
public class EleaveJjitemController extends BaseController {

    @Autowired
    private IEleaveJjitemService iEleaveJjitemService;

    /**
     * 保存工作交接项信息
     * @param eleaveJjitemReqDTO
     * @return
     */
    @PostMapping("/save")
    @ResponseBody
    private Result<Boolean> save(@Valid @RequestBody EleaveJjitemReqDTO eleaveJjitemReqDTO){
		EleaveJjitem queryBean = new EleaveJjitem();
		BeanUtils.copyProperties(eleaveJjitemReqDTO,queryBean);
        return success(iEleaveJjitemService.save(queryBean));
    }

    /**
     * 批量保存工作交接项信息
     * @param entityList
     * @return
     */
    @PostMapping("/save/batch")
    @ResponseBody
    private Result<Boolean> save(@RequestBody List<EleaveJjitemReqDTO> entityList){

		List<EleaveJjitem> newQueryList = new ArrayList<>();
		EleaveJjitem eleaveJjitem;
		for(EleaveJjitemReqDTO element :entityList) {
			eleaveJjitem = new EleaveJjitem();
			BeanUtils.copyProperties(element,eleaveJjitem);
			newQueryList.add(eleaveJjitem);
		}

        return success(iEleaveJjitemService.saveBatchEleaveJjitem(newQueryList));
    }
}
