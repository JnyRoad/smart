package com.tce.smart.data.controller.ehrview;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.CvwCcdAllowRuleDTO;
import com.tce.smart.ehrview.core.entity.CvwCcdAllowRule;
import com.tce.smart.ehrview.core.service.CvwCcdAllowRuleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器
 *
 * 补贴计算规则
 * @author qipei
 *
 */

@RestController
@RequestMapping("/cd/allow/rule")
public class CvwCcdAllowRuleController extends BaseController {

	@Autowired
	private  CvwCcdAllowRuleService  cvwCcdAllowRuleService;

	@SysLog("根据补贴计算规则id查询补贴计算规则")
	@Inner
	@OpenApi("server")
    @GetMapping("/get")
    public Result<CvwCcdAllowRuleDTO> getById(@RequestParam("id") String id){
		CvwCcdAllowRule cvwCcdAllowRule = cvwCcdAllowRuleService.getById(id);
		CvwCcdAllowRuleDTO cvwCcdAllowRuleDTO = new CvwCcdAllowRuleDTO();
		BeanUtils.copyProperties(cvwCcdAllowRule,cvwCcdAllowRuleDTO);
        return success(cvwCcdAllowRuleDTO);
    }

	@SysLog("根据补贴计算规则title查询补贴计算规则")
	@Inner
	@OpenApi("server")
	@GetMapping("/get/byTitle")
	public Result<CvwCcdAllowRuleDTO> getByTitle(@RequestParam("title") String title){

		CvwCcdAllowRule cvwCcdAllowRule = cvwCcdAllowRuleService.getByTitle(title);
		CvwCcdAllowRuleDTO cvwCcdAllowRuleDTO = new CvwCcdAllowRuleDTO();
		BeanUtils.copyProperties(cvwCcdAllowRule,cvwCcdAllowRuleDTO);
        return success(cvwCcdAllowRuleDTO);
	}
}
