package com.tce.smart.data.controller.temporary;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.data.api.dto.temporary.req.EbgEducationRegisterReqDTO;
import com.tce.smart.temporary.core.entity.EbgEducationRegister;
import com.tce.smart.temporary.core.service.IEbgEducationRegisterService;
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
@RequestMapping("/ebgEducationRegister")
public class EbgEducationRegisterController extends BaseController {

	@Autowired
    private IEbgEducationRegisterService iEbgEducationRegisterService;

    /**
     * 保存教育经历
     * @param ebgEducationRegisterReqDTO
     * @return
     */
    @PostMapping("/save")
    @ResponseBody
    private Result<Boolean> save(@RequestBody EbgEducationRegisterReqDTO ebgEducationRegisterReqDTO){
		EbgEducationRegister qeuryEbgEducationRegister = new EbgEducationRegister();
		BeanUtils.copyProperties(ebgEducationRegisterReqDTO,qeuryEbgEducationRegister);
        return success(iEbgEducationRegisterService.save(qeuryEbgEducationRegister));
    }
}
