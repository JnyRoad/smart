package com.tce.smart.app.controller.fore;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.service.fore.ApproveListService;
import com.tce.smart.app.vo.fore.ApproveListRequestVO;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import lombok.AllArgsConstructor;

/**
 * 待审批 控制器
 * @author 王艳勇
 *
 */

@RestController
@AllArgsConstructor
public class ApproveListController extends BaseController{

    private ApproveListService approveListService;

	/**
     * 待审批
     * @param approveListRequestVO
     * @return
     */
	@PostMapping("/approve/record")
	public Result getProcessRecord(@RequestBody ApproveListRequestVO approveListRequestVO) {
	    return approveListService.getProcessRecord(approveListRequestVO);
	}

}
