package com.tce.smart.data.controller.ehrview;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.LvwLeavetypeDTO;
import com.tce.smart.ehrview.core.entity.LvwLeavetype;
import com.tce.smart.ehrview.core.service.ILvwLeavetypeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@RestController
@RequestMapping("/lvwLeavetype")
public class LvwLeavetypeController extends BaseController {

	@Autowired
	private  ILvwLeavetypeService iLvwLeavetypeService;

    @Inner
    @OpenApi("server")
    @GetMapping("/info")
    public Result<LvwLeavetypeDTO> getById(@RequestParam("id") Integer id){
        LvwLeavetype byId = iLvwLeavetypeService.getById(id);
		LvwLeavetypeDTO lvwLeavetypeDTO = new LvwLeavetypeDTO();
		if(Objects.nonNull(byId)) {
			BeanUtils.copyProperties(byId,lvwLeavetypeDTO);
			return success(lvwLeavetypeDTO);
		}
		return null;
    }

}
