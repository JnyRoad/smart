package com.tce.smart.data.controller.ehrview;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.ehrview.req.OvwYsConComanyReqDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsConComanyRespDTO;
import com.tce.smart.ehrview.core.service.IOvwYsConComanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/***
 * description: 合同签约单位控制器 <br>
 * date: 2019/11/27 14:00 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@RestController
@RequestMapping("/ys/con/comany")
public class OvwYsConComanyController extends BaseController {

	@Autowired
	private IOvwYsConComanyService iOvwYsConComanyService;

	/**
	 * 根据title查询列表
	 *
	 * @param ovwYsConComany ovwYsConComany
	 * @return Result<List < OvwYsConComany>>
	 */
	@Inner
	@OpenApi("server")
	@PostMapping("/getByTitle")
	public Result<List<OvwYsConComanyRespDTO>> getByTitle(@RequestBody OvwYsConComanyReqDTO ovwYsConComany) {
		return success(iOvwYsConComanyService.getByTitle(ovwYsConComany.getTitle()),OvwYsConComanyRespDTO.class);
	}

	/**
	 * 根据compId查询
	 *
	 * @param compId compId
	 * @return Result<OvwYsConComany>
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/getByCompId/{compId}")
	public Result<OvwYsConComanyRespDTO> getByCompId(@PathVariable("compId") Integer compId) {
		return success(iOvwYsConComanyService.getByCompId(compId),OvwYsConComanyRespDTO.class);
	}

}
