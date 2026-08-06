package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.tce.smart.admin.api.entity.SysMoveDataTask;
import com.tce.smart.admin.api.feign.RemoteMoveDataTaskService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.DateMoveModuleEnum;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.ext.MoveDataTaskExt;
import com.tce.smart.platform.core.service.SmtMsgRecordService;
import com.tce.smart.schedule.service.platform.IMoveHisDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图片服务接口
 *
 * @author mckaywu
 * @date 2019-05-30 11:46:36
 */
@Service
@Slf4j
public class MoveHisDataServiceImpl implements IMoveHisDataService {

	@Autowired
	private RemoteMoveDataTaskService remoteMoveDataTaskService;

	@Autowired
	private SmtMsgRecordService smtMsgRecordService;


	@Override
	public void moveHisData() {
		Result<List<SysMoveDataTask>> rsList = remoteMoveDataTaskService
				.getTaskTableList(DateMoveModuleEnum.PLATFORM.getCode(), SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("remoteMoveDataTaskService.getTaskTableList rs={}", rsList);
		if (rsList.isSuccess() && CollectionUtil.isNotEmpty(rsList.getData())) {
			List<SysMoveDataTask> tableTaskList = rsList.getData();
			// 处理转移数据
			if (CollectionUtil.isNotEmpty(tableTaskList)) {
				MoveDataTaskExt moveDataTaskExt = null;
				for (SysMoveDataTask element : tableTaskList) {
					moveDataTaskExt = new MoveDataTaskExt();
					BeanUtils.copyProperties(element, moveDataTaskExt);
					smtMsgRecordService.processData(moveDataTaskExt);
				}
//					tableTaskList.forEach(element -> smtMsgRecordService.processData(element));
			}
		}
	}


}
