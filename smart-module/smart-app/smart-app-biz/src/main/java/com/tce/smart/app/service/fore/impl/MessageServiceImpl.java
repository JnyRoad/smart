package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.service.fore.MessageService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.SmtMsgRecordDTO;
import com.tce.smart.platform.api.dto.req.QueryAppMsgRecReqDTO;
import com.tce.smart.platform.api.dto.resp.QueryAppMsgRecRespDTO;
import com.tce.smart.platform.api.feign.RemoteAppPushRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * App消息记录服务实现类
 *
 * @author mkwu
 * @date 2019-07-07
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

	@Autowired
	RemoteAppPushRecordService remoteAppPushRecordService;

	@Override
	public Page<SmtMsgRecordDTO> getAppMsgList(Long current, Long size, String deviceNo) {
		QueryAppMsgRecReqDTO queryAppMsgRecDTO = new QueryAppMsgRecReqDTO();
		queryAppMsgRecDTO.setBadge(SecurityUtils.getUser().getUsername());
		queryAppMsgRecDTO.setDeviceNo(deviceNo);

		Result<Page<SmtMsgRecordDTO>> rs = remoteAppPushRecordService.lisetAppMsgByPge(current, size, queryAppMsgRecDTO,
				SecurityConstants.FROM_IN);
		log.info("remoteAppPushRecordService.lisetAppMsgByPge.rs = {}", rs);
		return rs.getData();
	}

	@Override
	public Boolean changeRecordToRead(Integer recordId) {
		if(ObjectUtil.isNotNull(recordId))
		{
			log.info("remoteAppPushRecordService.lisetAppMsgByPge.param = {}", recordId);
			Result<Boolean> rs = remoteAppPushRecordService.changeRecordToRead(recordId,SecurityConstants.FROM_IN);
			log.info("remoteAppPushRecordService.lisetAppMsgByPge.rs = {}", rs);
			if(rs.isSuccess()) {
				return rs.getData();
			}
			else {
				return Boolean.FALSE;
			}
		}
		else
		{
			return Boolean.FALSE;
		}
	}

	@Override
	public QueryAppMsgRecRespDTO countAppMsg(String deviceNo) {
		QueryAppMsgRecReqDTO queryAppMsgRecDTO = new QueryAppMsgRecReqDTO();
		queryAppMsgRecDTO.setBadge(SecurityUtils.getUser().getUsername());
		queryAppMsgRecDTO.setDeviceNo(deviceNo);
		Result<QueryAppMsgRecRespDTO> rs = remoteAppPushRecordService.countAppMsg(queryAppMsgRecDTO,SecurityConstants.FROM_IN);
		log.info("remoteAppPushRecordService.countAppMsg.rs = {}", rs);
		if(rs.isSuccess()) {
			return rs.getData();
		}
		else {
			return null;
		}
	}

	@Override
	public Boolean deleteMsg(Integer recordId) {
		Result<Boolean> rs = remoteAppPushRecordService.deleteMsg(recordId,SecurityConstants.FROM_IN);
		log.info("remoteAppPushRecordService.deleteMsg.rs = {}", rs);
		if(rs.isSuccess()) {
			return rs.getData();
		}
		else {
			return Boolean.FALSE;
		}
	}

	@Override
	public Boolean changeAllRecordToRead(String deviceNo) {
		// TODO Auto-generated method stub

		QueryAppMsgRecReqDTO queryAppMsgRecDTO = new QueryAppMsgRecReqDTO();
		queryAppMsgRecDTO.setBadge(SecurityUtils.getUser().getUsername());
		queryAppMsgRecDTO.setDeviceNo(deviceNo);
		Result<Boolean> rs = remoteAppPushRecordService.changeAllRecordToRead(queryAppMsgRecDTO,SecurityConstants.FROM_IN);
		log.info("remoteAppPushRecordService.changeAllRecordToRead.rs = {}", rs);
		if(rs.isSuccess()) {
			return rs.getData();
		}
		else {
			return Boolean.FALSE;
		}
	}

	@Override
	public Boolean deleteAllMsg(String deviceNo) {
		// TODO Auto-generated method stub

		QueryAppMsgRecReqDTO queryAppMsgRecDTO = new QueryAppMsgRecReqDTO();
		queryAppMsgRecDTO.setBadge(SecurityUtils.getUser().getUsername());
		queryAppMsgRecDTO.setDeviceNo(deviceNo);
		Result<Boolean> rs = remoteAppPushRecordService.deleteAllMsg(queryAppMsgRecDTO,SecurityConstants.FROM_IN);
		log.info("remoteAppPushRecordService.deleteAllMsg.rs = {}", rs);
		if(rs.isSuccess()) {
			return rs.getData();
		}
		else {
			return Boolean.FALSE;
		}
	}

}
