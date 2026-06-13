package com.tce.smart.data.controller.ehrview;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsCallOwanceCancelAllRespDTO;
import com.tce.smart.ehrview.core.entity.OvwYsCallOwanceCancelAll;
import com.tce.smart.ehrview.core.service.IOvwYsCallOwanceCancelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 外宿审批撤销控制器
 * @author QIPEI
 *
 */
@RestController
@RequestMapping("/ovwYsCallOwanceCancel")
public class OvwYsCallOwanceCancelController extends BaseController {

	@Autowired
	private  IOvwYsCallOwanceCancelService service;

	@SysLog("查询外宿审批撤销记录")
	@Inner
    @GetMapping("/get")
    public Result<List<OvwYsCallOwanceCancelAllRespDTO>> getInfo(@RequestParam("badge") String badge, @RequestParam("xtype") Integer xtype, @RequestParam("begindate") String begindate){

		List<OvwYsCallOwanceCancelAll> list = service.list(Wrappers.<OvwYsCallOwanceCancelAll> query().lambda().eq(OvwYsCallOwanceCancelAll::getBadge, badge).eq(OvwYsCallOwanceCancelAll::getXtype, xtype)
				.eq(OvwYsCallOwanceCancelAll::getBegindate, begindate));
        return success(list, OvwYsCallOwanceCancelAllRespDTO.class);
    }

}
