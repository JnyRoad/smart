package com.tce.smart.data.controller.temporary;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.temporary.req.EbgFamilyRegisterReqDTO;
import com.tce.smart.temporary.core.entity.EbgFamilyRegister;
import com.tce.smart.temporary.core.service.IEbgFamilyRegisterService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Controller
@RequestMapping("/ebgFamilyRegister")
public class EbgFamilyRegisterController extends BaseController {

	@Autowired
    private IEbgFamilyRegisterService iEbgFamilyRegisterService;

    /**
     * 保存家庭背景
     * @param ebgFamilyRegisterReqDTO ebgFamilyRegisterReqDTO
     * @return
     */
    @Inner
    @OpenApi("server")
    @PostMapping("/internal/save")
    @ResponseBody
    private Result<Boolean> save(@RequestBody EbgFamilyRegisterReqDTO ebgFamilyRegisterReqDTO){

		EbgFamilyRegister queryBean = new EbgFamilyRegister();
		BeanUtils.copyProperties(ebgFamilyRegisterReqDTO,queryBean);
        return success(iEbgFamilyRegisterService.save(queryBean));
    }
}
