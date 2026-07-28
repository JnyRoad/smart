package com.tce.smart.data.controller.temporary;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.temporary.req.EbgeJavoidanceRegisterReqDTO;
import com.tce.smart.temporary.core.entity.EbgeJavoidanceRegister;
import com.tce.smart.temporary.core.service.IEbgeJavoidanceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author QIPEI
 *
 */
@Controller
@RequestMapping("/ebgJavoidanceRegister")
public class EbgeJavoidanceController extends BaseController {


	@Autowired
	private IEbgeJavoidanceService service;

    /**
     * 保存亲属关系
     * @param ebgeJavoidanceRegisterReqDTO
     * @return
     */
    @Inner
    @OpenApi("server")
    @PostMapping("/internal/save")
    @ResponseBody
    private Result<Boolean> save(@RequestBody EbgeJavoidanceRegisterReqDTO ebgeJavoidanceRegisterReqDTO){
		EbgeJavoidanceRegister queryBean = new EbgeJavoidanceRegister();
		BeanUtils.copyProperties(ebgeJavoidanceRegisterReqDTO,queryBean);
        return success(service.save(queryBean));
    }
}
