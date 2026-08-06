package com.tce.smart.data.controller.temporary;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.temporary.resp.OcompanyRespDTO;
import com.tce.smart.temporary.core.entity.Ocompany;
import com.tce.smart.temporary.core.service.OcompanyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器
 * @author QIPEI
 *
 */

@RestController
@RequestMapping("/company")
public class OcompanyController extends BaseController {

	@Autowired
	private OcompanyService service;

	@Inner
	@OpenApi("server")
	@GetMapping("/internal/getByComId")
	public Result<OcompanyRespDTO> getByComId(@RequestParam("compId") Integer compId) {
		Ocompany ocompany = service.getByComId(compId);
		OcompanyRespDTO qcompanyRespDTO = new OcompanyRespDTO();
		BeanUtils.copyProperties(ocompany, qcompanyRespDTO);
		return success(qcompanyRespDTO);
	}



}
