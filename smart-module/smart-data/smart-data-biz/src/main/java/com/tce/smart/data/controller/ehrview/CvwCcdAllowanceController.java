package com.tce.smart.data.controller.ehrview;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.CvwCcdAllowanceDTO;
import com.tce.smart.ehrview.core.entity.CvwCcdAllowance;
import com.tce.smart.ehrview.core.service.CvwCcdAllowanceService;
import org.springframework.beans.BeanUtils;
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
 * 补贴类型
 * @author qipei
 * @since 2019-06-13
 */
@RestController
@RequestMapping("/cd/allowance")
public class CvwCcdAllowanceController  extends BaseController {

	@Autowired
	private CvwCcdAllowanceService cvwCcdAllowanceService;

		@SysLog("根基补贴名称查询补贴信息")
		@Inner
		@OpenApi("server")
	    @GetMapping("/get")
	    public Result<CvwCcdAllowanceDTO> getByName(@RequestParam("allowanceName") String allowanceName){
			CvwCcdAllowance cvwCcdAllowance = cvwCcdAllowanceService.getByName(allowanceName);
			CvwCcdAllowanceDTO cvwCcdAllowanceDTO = new CvwCcdAllowanceDTO();
			BeanUtils.copyProperties(cvwCcdAllowance,cvwCcdAllowanceDTO);
	        return success(cvwCcdAllowanceDTO);
	    }

}
