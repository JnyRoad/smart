package com.tce.smart.data.controller.temporary;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.temporary.req.EbgWorkingRegisterReqDTO;
import com.tce.smart.temporary.core.entity.EbgWorkingRegister;
import com.tce.smart.temporary.core.service.IEbgWorkingRegisterService;
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
@RequestMapping("/ebgWorkingRegister")
public class EbgWorkingRegisterController extends BaseController {

	@Autowired
    private IEbgWorkingRegisterService iEbgWorkingRegisterService;

    /**
     * 保存工作经历
     * @param ebgWorkingRegisterReqDTO
     * @return
     */
    @Inner
    @OpenApi("server")
    @PostMapping("/internal/save")
    @ResponseBody
    private Result<Boolean> save(@RequestBody EbgWorkingRegisterReqDTO ebgWorkingRegisterReqDTO){
		EbgWorkingRegister ebgWorkingRegister = new EbgWorkingRegister();
		BeanUtils.copyProperties(ebgWorkingRegisterReqDTO,ebgWorkingRegister);
        return success(iEbgWorkingRegisterService.save(ebgWorkingRegister));
    }
}
