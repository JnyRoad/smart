package com.tce.smart.data.controller.ehrview;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.LvwLcdLeavetypeDTO;
import com.tce.smart.ehrview.core.entity.LvwLcdLeavetype;
import com.tce.smart.ehrview.core.service.ILvwLcdLeavetypeService;
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
@RequestMapping("/lvw/lvwLcdLeavetype")
public class LvwLcdLeavetypeController  extends BaseController {
	 @Autowired
	    private ILvwLcdLeavetypeService iLvwLcdLeavetypeService;
		@Inner
		@OpenApi("server")
	    @GetMapping("/info")
	    public Result<LvwLcdLeavetypeDTO> info(@RequestParam("id") Integer id){
			LvwLcdLeavetype lvwLcdLeavetype = iLvwLcdLeavetypeService.getOne(
					Wrappers. <LvwLcdLeavetype>query().lambda()
	                .eq(LvwLcdLeavetype::getId, id)
	        );

			LvwLcdLeavetypeDTO lvwLcdLeavetypeDTO = new LvwLcdLeavetypeDTO();
			BeanUtils.copyProperties(lvwLcdLeavetype,lvwLcdLeavetypeDTO);
	        return success(lvwLcdLeavetypeDTO);
	    }
}
