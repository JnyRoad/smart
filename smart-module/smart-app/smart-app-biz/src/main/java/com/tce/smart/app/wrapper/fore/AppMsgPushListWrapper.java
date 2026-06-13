package com.tce.smart.app.wrapper.fore;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.tce.smart.app.vo.fore.AppMsgPushListVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.SmtMsgRecordDTO;
import io.netty.util.internal.StringUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class AppMsgPushListWrapper extends BaseWrapper<SmtMsgRecordDTO, AppMsgPushListVo> {

	@Override
	protected AppMsgPushListVo warp(SmtMsgRecordDTO smtMsgRecord) throws IOException {
		AppMsgPushListVo vo = new AppMsgPushListVo();
		vo.setRecordId(smtMsgRecord.getId());
		String remark2 = smtMsgRecord.getRemark2();
		String[] parmsStr = null;
		Map<String, String> tempMap = new HashMap<String, String>();
		for (String temStr : remark2.split("\\|\\|")) {
			if (temStr.contains("=")) {
				parmsStr = temStr.split("=");
				tempMap.put(parmsStr[0], parmsStr[1]);
			}
		}
		String businessType = tempMap.get("businessType");
		vo.setBusinessType(StringUtil.isNullOrEmpty(businessType) ? -1 : Integer.valueOf(businessType));
		vo.setBusinessId(tempMap.get("businessId"));
		vo.setMsgTitle(smtMsgRecord.getRemark1());
		vo.setMsgContent(smtMsgRecord.getMsgContent());
		vo.setReadState(smtMsgRecord.getReadState());
		vo.setDeleteState(smtMsgRecord.getDeleteState());
		vo.setExtraParam(smtMsgRecord.getRemark2());//额外参数
		String createDate = "";
		String createTime = "";
		if (Objects.nonNull(smtMsgRecord.getCreateTime())) {
			ZonedDateTime zonedDateTime = smtMsgRecord.getCreateTime().toLocalDate().atStartOfDay(ZoneId.systemDefault());
			Date dbCreatDate = Date.from(zonedDateTime.toInstant());
			Date nowDate = Calendar.getInstance().getTime();
			long betweenRs = DateUtil.between(nowDate, dbCreatDate, DateUnit.DAY);
//			if (betweenRs == 0l) {
//				createDate = "今天";
//			} else if (betweenRs == 1l) {
//				createDate = "昨天";
//			} else {
				createDate = smtMsgRecord.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		//	}

			createTime = smtMsgRecord.getCreateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
		}

		vo.setCreateDate(createDate);
		vo.setCreateTime(createTime);
		return vo;
	}
}
