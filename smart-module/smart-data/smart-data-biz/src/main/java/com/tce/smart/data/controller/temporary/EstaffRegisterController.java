package com.tce.smart.data.controller.temporary;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.temporary.req.EstaffRegisterReqDTO;
import com.tce.smart.temporary.core.entity.EstaffRegister;
import com.tce.smart.temporary.core.service.IEstaffRegisterService;
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
@RequestMapping("/estaffRegister")
public class EstaffRegisterController extends BaseController {

	@Autowired
    private IEstaffRegisterService iEstaffRegisterService;

    /**
     * 保存入职信息
     * @param estaffRegisterReqDTO
     * @return
     */
    @Inner
    @OpenApi("server")
    @PostMapping("/internal/save")
    @ResponseBody
    private Result<Boolean> save(@RequestBody EstaffRegisterReqDTO estaffRegisterReqDTO){
		EstaffRegister  saveBean = new EstaffRegister();
		BeanUtils.copyProperties(estaffRegisterReqDTO,saveBean);
        return success(iEstaffRegisterService.save(saveBean));
    }
}
