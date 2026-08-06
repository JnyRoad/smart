package com.tce.smart.data.controller.ehrview;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.req.CInterFaceBenSupplyReqDTO;
import com.tce.smart.ehrview.core.entity.CInterFaceBenSupply;
import com.tce.smart.ehrview.core.service.CInterFaceBenSupplyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/cinter/supply")
@Slf4j
public class CInterFaceBenSupplyController extends BaseController {

	@Autowired
	private CInterFaceBenSupplyService cInterFaceBenSupplyService;

	/**
	 * 推送水电扣费到EHR
	 * @param cInterFaceBenSupplyReqDTO
	 * @return
	 * @throws ParseException
	 */
    @Inner
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody CInterFaceBenSupplyReqDTO cInterFaceBenSupplyReqDTO) throws ParseException{
		CInterFaceBenSupply cInterFaceBenSupply = new CInterFaceBenSupply();
		cInterFaceBenSupply.setBadge(cInterFaceBenSupplyReqDTO.getBadge());
		cInterFaceBenSupply.setAmount(cInterFaceBenSupplyReqDTO.getAmount());
		cInterFaceBenSupply.setObject(cInterFaceBenSupplyReqDTO.getObject());
		cInterFaceBenSupply.setIsDisPose("N");
        return success(cInterFaceBenSupplyService.save(cInterFaceBenSupply));
    }

}
