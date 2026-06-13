package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.NumberConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.core.dto.MsgTempDTO;
import com.tce.smart.platform.core.entity.SmtMsgTemp;
import com.tce.smart.platform.core.mapper.SmtMsgTempMapper;
import com.tce.smart.platform.core.service.SmtMsgTempPersonService;
import com.tce.smart.platform.core.service.SmtMsgTempService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Li.JiaJun
 * @since 2022/9/26 14:31
 */
@Service
public class SmtMsgTempServiceImpl extends ServiceImpl<SmtMsgTempMapper, SmtMsgTemp> implements SmtMsgTempService {

	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	@Autowired
	private SmtMsgTempPersonService tempPersonService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean save(MsgTempDTO dto) {
		SmtMsgTemp msgTemp = BeanUtils.transform(SmtMsgTemp.class, dto);
		msgTemp.setParkId(xcParkId);
		msgTemp.setMsgType(1);
		if (Objects.isNull(msgTemp.getId()) && Objects.nonNull(dto)) {
			checkExist(dto.getParkId());
		}
		this.saveOrUpdate(msgTemp);
		return tempPersonService.save(msgTemp.getId(), dto.getPersonList());
	}

	@Override
	public List<SmtMsgTemp> getList() {
		return this.list(Wrappers.<SmtMsgTemp>lambdaQuery()
				.eq(SmtMsgTemp::getMsgType, NumberConstants.ONE));
	}

	@Override
	public List<String> getBadgeByParkId(Integer parkId) {
		SmtMsgTemp temp = this.getOne(Wrappers.<SmtMsgTemp>lambdaQuery()
				.eq(SmtMsgTemp::getParkId, parkId)
				.eq(SmtMsgTemp::getMsgType, NumberConstants.ONE));
		if (Objects.nonNull(temp)) {
			return tempPersonService.getByTempId(temp.getId());
		}
		return Collections.emptyList();
	}

	private void checkExist(Integer parkId) {
		boolean isExist = this.count(Wrappers.<SmtMsgTemp>lambdaQuery()
				.eq(SmtMsgTemp::getParkId, parkId)
				.eq(SmtMsgTemp::getMsgType, NumberConstants.ONE)) > 0;
		if (isExist) {
			throw new SmartException("该园区已存在消息模板，无法新增");
		}
	}
}
