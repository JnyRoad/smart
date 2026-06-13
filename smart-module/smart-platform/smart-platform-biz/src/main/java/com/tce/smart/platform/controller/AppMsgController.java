package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.SpringContextHolder;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.resp.SmtMsgRecordRespDTO;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.dto.QueryAppMsgRecDTO;
import com.tce.smart.platform.core.dto.QueryMsgDTO;
import com.tce.smart.platform.core.dto.TestSendAppMsgDTO;
import com.tce.smart.platform.core.entity.SmtMsgRecord;
import com.tce.smart.platform.core.service.SmtMsgRecordService;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.platform.core.vo.MsgInfoVO;
import com.tce.smart.platform.core.vo.MsgStateVO;
import com.tce.smart.platform.core.vo.MsgTemplateVO;
import com.tce.smart.platform.core.vo.QueryAppMsgRecVO;
import com.tce.smart.platform.service.IAppMsgPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appmsg")
public class AppMsgController extends BaseController {

	@Autowired
	private IAppMsgPushService appMsgPushService;
	@Autowired
	private SmtMsgRecordService smtMsgRecordService;
	@Autowired
	private SmtMsgTemplateService smtMsgTemplateService;

	/**
	 * 设置App消息为已读
	 *
	 * @param recordId 消息记录ID
	 * @return
	 */
	@GetMapping("/update/read")
	public Result<Boolean> changeRecordToRead(@RequestParam("recordId") Integer recordId) {
		return new Result<Boolean>(appMsgPushService.changeRecordToRead(recordId));
	}

	@PostMapping("/update/all/read")
	public Result<Boolean> changeAllRecordToRead(@RequestBody QueryAppMsgRecDTO queryAppMsgRecDTO) {
		return new Result<Boolean>(appMsgPushService.changeAllRecordToRead(queryAppMsgRecDTO));
	}

	/**
	 * 统计App消息数
	 *
	 * @param queryAppMsgRecDTO App消息推送记录查询条件
	 * @return
	 */
	@PostMapping("/count/app")
	public Result<QueryAppMsgRecVO> countAppMsg(@RequestBody QueryAppMsgRecDTO queryAppMsgRecDTO) {
		return new Result<>(appMsgPushService.countAppMsg(queryAppMsgRecDTO));
	}

	/**
	 * 分页查询
	 *
	 * @param page      分页对象
	 * @param queryAppMsgRecDTO App消息推送记录查询条件
	 * @return
	 */
	@PostMapping("/page")
	public Result<IPage<SmtMsgRecord>> getSmtDevicePage(Page<?> page,
			@RequestBody QueryAppMsgRecDTO queryAppMsgRecDTO) {
		return new Result<>(appMsgPushService.queryAppMsgList(page, queryAppMsgRecDTO));
	}

	/**
	 * 测试App消息推送
	 *
	 * @param testSendAppMsgDTO
	 * @return
	 */
	@PostMapping("/test/sendAppMsg")
	public Result sendAppMsg(@RequestBody TestSendAppMsgDTO testSendAppMsgDTO) {
		IAppMsgPushService iAppMsgPushService = SpringContextHolder
				.getBean(IAppMsgPushService.class);
		// 推送App消息
		AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
		appMsgPushDTO.setBadge(testSendAppMsgDTO.getBadge());
		appMsgPushDTO.setBussiessId(testSendAppMsgDTO.getBussiessId());
		appMsgPushDTO.setTemplateCode(testSendAppMsgDTO.getTempCode());
		appMsgPushDTO.setExtraParam(testSendAppMsgDTO.getExtraPara());// 扩展参数
		appMsgPushDTO.setUrl(testSendAppMsgDTO.getUrl());// URL
		return success("测试数据：" + iAppMsgPushService.pushAppMsg(appMsgPushDTO));
	}

	/**
	 * 删除App消息
	 *
	 * @param recordId 消息记录ID
	 * @return
	 */
	@PostMapping("/delete")
	public Result<Boolean> deleteMsg(@RequestParam("recordId") Integer recordId){
		return new Result<Boolean>(appMsgPushService.deleteMsg(recordId));
	}

	/**
	 * 删除所有App消息
	 *
	 * @param queryAppMsgRecDTO 消息记录ID
	 * @return
	 */
	@PostMapping("/delete/all")
	public Result<Boolean> deleteAllMsg(@RequestBody QueryAppMsgRecDTO queryAppMsgRecDTO){
		return new Result<Boolean>(appMsgPushService.deleteAllMsg(queryAppMsgRecDTO));
	}

	/**
	 * 根据查询条件获得消息列表
	 *
	 * @param page 分页参数
	 * @param queryMsgDTO 查询条件
	 * @return
	 */
	@PostMapping("/query/page")
	public Result getMsgInfo(Page<?> page, @RequestBody(required = false) QueryMsgDTO queryMsgDTO) {
		return success(smtMsgRecordService.getMsg(page, queryMsgDTO), SmtMsgRecordRespDTO.class);
	}

	/**
	 * 获得发送状态
	 *
	 * @return
	 */
	@GetMapping("/send/state")
	public Result<MsgStateVO> getSendState() {
		return new Result(appMsgPushService.getState());
	}

	/**
	 * 查询所有短信模板
	 * @return
	 */
	@GetMapping("/template/all")
	public Result getTemplate() {
		return success(smtMsgTemplateService.getMsgTemplate(), MsgTemplateVO.class);
	}

	/**
	 * 根据信息id获得信息内容
	 * @return
	 */
	@GetMapping("/query/{id}")
	public Result getMsgById(@PathVariable("id") Integer id) {
		return success(appMsgPushService.getMsgById(id), MsgInfoVO.class);
	}
}
