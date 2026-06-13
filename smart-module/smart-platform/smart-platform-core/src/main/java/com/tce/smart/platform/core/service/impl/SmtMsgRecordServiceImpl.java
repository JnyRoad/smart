package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.DateMoveOptEnum;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.core.dto.QueryAppMsgRecDTO;
import com.tce.smart.platform.core.dto.QueryMsgDTO;
import com.tce.smart.platform.core.entity.SmtMsgRecord;
import com.tce.smart.platform.core.entity.ext.MoveDataTaskExt;
import com.tce.smart.platform.core.mapper.SmtMsgRecordMapper;
import com.tce.smart.platform.core.service.SmtMsgRecordService;
import com.tce.smart.tool.enums.DeleteStatusEnum;
import com.tce.smart.tool.enums.MsgReadStateEnum;
import com.tce.smart.tool.enums.MsgTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 消息模板服务实现类
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:49:29
 */
@Service
@Slf4j
public class SmtMsgRecordServiceImpl extends ServiceImpl<SmtMsgRecordMapper, SmtMsgRecord>
		implements SmtMsgRecordService {

	@Override
	public Integer addRecord(SmtMsgRecord smtMsgRecord) {
		Integer pk = null;
		if (Objects.nonNull(smtMsgRecord)) {
			smtMsgRecord.setCreateTime(DateUtils.localDateTime());
			smtMsgRecord.setUpdateTime(DateUtils.localDateTime());
			baseMapper.insert(smtMsgRecord);
			pk = smtMsgRecord.getId();
		}

		return pk;
	}

	@Override
	public Integer updateRecordState(Integer id, Integer state, String remark) {
		SmtMsgRecord smtMsgRecord = this.getById(id);
		smtMsgRecord.setMsgState(state);
		if (StringUtils.isNotBlank(remark)) {
			smtMsgRecord.setMsgDesc(remark);
		}
		return baseMapper.updateById(smtMsgRecord);
	}

	@Override
	public IPage<SmtMsgRecord> listAppMsgByPage(Page<?> page, QueryAppMsgRecDTO queryAppMsgRecDTO) {
		if (Objects.isNull(queryAppMsgRecDTO) || StringUtils.isEmpty(queryAppMsgRecDTO.getDeviceNo())) {
			return null;
		}

		// 接收对象
		String msgObject = queryAppMsgRecDTO.getBadge() + "-" + queryAppMsgRecDTO.getDeviceNo();
		return this.baseMapper.listRecordByPage(page, MsgTypeEnum.MSG_3.getCode(), null, msgObject,
				queryAppMsgRecDTO.getStartTime(), queryAppMsgRecDTO.getEndTime());
	}

	@Override
	public Boolean updateRecordToRead(Integer recordId) {
		SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
		smtMsgRecord.setId(recordId);
		smtMsgRecord.setReadState(MsgReadStateEnum.READ.getCode());
		smtMsgRecord.setUpdateTime(LocalDateTime.now());
		return this.updateById(smtMsgRecord);
	}


	@Override
	public Boolean updateAllRecordToRead() {
		// TODO Auto-generated method stub
		return Boolean.FALSE;
	}


	@Override
	public Integer countAppMsgSuccess(QueryAppMsgRecDTO queryAppMsgRecDTO) {
		SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
		// 接收对象
		String msgObject = queryAppMsgRecDTO.getBadge() + "-" + queryAppMsgRecDTO.getDeviceNo();
		smtMsgRecord.setMsgObject(msgObject);
//		smtMsgRecord.setMsgState(SmsRecordSateEnum.SUCCESS.getCode());
		smtMsgRecord.setDeleteState(DeleteStatusEnum.NOT_DELETE.getCode());
		return this.baseMapper.countByCondition(MsgTypeEnum.MSG_3.getCode(), null, smtMsgRecord,
				queryAppMsgRecDTO.getStartTime(), queryAppMsgRecDTO.getEndTime());
	}

	@Override
	public Integer countAppMsgRead(QueryAppMsgRecDTO queryAppMsgRecDTO) {
		SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
		// 接收对象
		String msgObject = queryAppMsgRecDTO.getBadge() + "-" + queryAppMsgRecDTO.getDeviceNo();
		smtMsgRecord.setMsgObject(msgObject);
//		smtMsgRecord.setMsgState(SmsRecordSateEnum.SUCCESS.getCode());
		smtMsgRecord.setReadState(MsgReadStateEnum.READ.getCode());
		smtMsgRecord.setDeleteState(DeleteStatusEnum.NOT_DELETE.getCode());
		return this.baseMapper.countByCondition(MsgTypeEnum.MSG_3.getCode(), null, smtMsgRecord,
				queryAppMsgRecDTO.getStartTime(), queryAppMsgRecDTO.getEndTime());
	}

	@Override
	public Integer countAppMsgUnRead(QueryAppMsgRecDTO queryAppMsgRecDTO) {
		SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
		// 接收对象
		String msgObject = queryAppMsgRecDTO.getBadge() + "-" + queryAppMsgRecDTO.getDeviceNo();
		smtMsgRecord.setMsgObject(msgObject);
//		smtMsgRecord.setMsgState(SmsRecordSateEnum.SUCCESS.getCode());
		smtMsgRecord.setReadState(MsgReadStateEnum.UNREAD.getCode());
		smtMsgRecord.setDeleteState(DeleteStatusEnum.NOT_DELETE.getCode());
		return this.baseMapper.countByCondition(MsgTypeEnum.MSG_3.getCode(), null, smtMsgRecord,
				queryAppMsgRecDTO.getStartTime(), queryAppMsgRecDTO.getEndTime());
	}

	@Override
	@Transactional
	public void processData(MoveDataTaskExt moveDataTaskExt) {
		if (Objects.isNull(moveDataTaskExt)) {
			return;
		}

		Integer optTytpe = moveDataTaskExt.getOptType();
		String srcTable = moveDataTaskExt.getSrcTable();
		String destTable = moveDataTaskExt.getDestTable();
		String dateColumn = moveDataTaskExt.getDateColumnName();
		try {
			Date endDate = DateUtil.offsetMonth(Calendar.getInstance().getTime(), -moveDataTaskExt.getRetainMonth());

			// 数据转移
			if (DateMoveOptEnum.MOVE.getCode().equals(optTytpe)) {
				this.baseMapper.moveData(srcTable, destTable, dateColumn, endDate);
				int count = this.baseMapper.deleteData(srcTable, dateColumn, endDate);
				log.info("move task deleteData===count={}", count);
			}
			// 数据删除
			else if (DateMoveOptEnum.DELETE.getCode().equals(optTytpe)) {
				int count = this.baseMapper.deleteData(srcTable, dateColumn, endDate);
				log.info("move task deleteData===count={}", count);
			}
		} catch (Exception e) {
			log.error("转移数据异常", e);
		}
	}

	@Override
	public Boolean deleteMsg(Integer recordId) {
		SmtMsgRecord smtMsgRecord = new SmtMsgRecord();
		smtMsgRecord.setId(recordId);
		smtMsgRecord.setDeleteState(DeleteStatusEnum.IS_DELETE.getCode());
		smtMsgRecord.setUpdateTime(LocalDateTime.now());
		return this.updateById(smtMsgRecord);
	}

	@Override
	public IPage<SmtMsgRecord> getMsg(Page page, QueryMsgDTO queryMsgDTO) {
		return baseMapper.getMsgInfo(page, queryMsgDTO);
	}

	@Override
	public Boolean updateAllRecordToRead(QueryAppMsgRecDTO queryAppMsgRecDTO) {
		// TODO Auto-generated method stub
		// 接收对象
		String msgObject = queryAppMsgRecDTO.getBadge() + "-" + queryAppMsgRecDTO.getDeviceNo();
		Integer type = MsgTypeEnum.MSG_3.getCode();
		Integer readState = MsgReadStateEnum.UNREAD.getCode();
		List<SmtMsgRecord> allList = this.baseMapper.queryAllRecordToUpdate(msgObject, type, readState);
		for (SmtMsgRecord smtMsgRecord : allList) {

			smtMsgRecord.setReadState(MsgReadStateEnum.READ.getCode());
			smtMsgRecord.setUpdateTime(LocalDateTime.now());
			this.updateById(smtMsgRecord);
		}
		return true;
	}

	@Override
	public Boolean deleteAllMsg(QueryAppMsgRecDTO queryAppMsgRecDTO) {
		// TODO Auto-generated method stub
		String msgObject = queryAppMsgRecDTO.getBadge() + "-" + queryAppMsgRecDTO.getDeviceNo();
		Integer type = MsgTypeEnum.MSG_3.getCode();
		List<SmtMsgRecord> allList = this.baseMapper.queryAllRecordToUpdate(msgObject, type, null);
		for (SmtMsgRecord smtMsgRecord : allList) {
			smtMsgRecord.setDeleteState(DeleteStatusEnum.IS_DELETE.getCode());
			smtMsgRecord.setUpdateTime(LocalDateTime.now());
			this.updateById(smtMsgRecord);
		}
		return true;
	}


}
