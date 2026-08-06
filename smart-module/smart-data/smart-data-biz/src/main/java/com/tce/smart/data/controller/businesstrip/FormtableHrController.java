package com.tce.smart.data.controller.businesstrip;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.businesstrip.core.entity.VwHRMResource;
import com.tce.smart.businesstrip.core.service.HrFormtableMainService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.businesstrip.req.VwHRMResourceReqDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.VwHRMResourceRespDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * 出差 数据库查询
 *
 * @author ly
 * @date 2019-06-28
 */
@RestController
@RequestMapping("/formtable")
public class FormtableHrController extends BaseController {

	@Autowired
	private HrFormtableMainService hrFormtableMainService;

	/**
	 * 获取人员
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/infoPerson")
	public Result<VwHRMResourceRespDTO> infoPerson(VwHRMResourceReqDTO vwHRMResource) {
		VwHRMResource qeuryVwHRMResource = new VwHRMResource();
		BeanUtils.copyProperties(vwHRMResource,qeuryVwHRMResource);

		VwHRMResource vwHRMResourceRs = hrFormtableMainService.getOne(Wrappers.query(qeuryVwHRMResource));
		VwHRMResourceRespDTO vwHRMResourceRespDTO = new VwHRMResourceRespDTO();
		BeanUtils.copyProperties(vwHRMResourceRs,vwHRMResourceRespDTO);
		return success(vwHRMResourceRespDTO);
	}
}
