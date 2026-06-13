package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.BridgeListenerDTO;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: IntergrationTerminalController
 * @date: 2020-07-02 17:08
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/inner/terminal")
public class IntergrationTerminalController extends BaseController {

	/**
	 * 接收人证比对机记录通知
	 * @param bridgeListenerDTO
	 * @return
	 */
	@Inner
	@PostMapping("/log/reply")
	public Result<Boolean> replyOfTerminal(@RequestBody BridgeListenerDTO bridgeListenerDTO){
		log.info("接收人证比对机记录{}",bridgeListenerDTO.getContent());
		if(StringUtils.isBlank(bridgeListenerDTO.getContent())){
			throw new TCEException("人证比对机记录收到数据为空");
		}
		//return success(terminalLogBizService.handTerminalLog(bridgeListenerDTO.getContent()));
		return success();
	}
}
