package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.core.entity.SmtNoticeSwitch;
import com.tce.smart.platform.core.mapper.SmtNoticeSwitchMapper;
import com.tce.smart.platform.service.SmtNoticeSwitchService;
import com.tce.smart.tool.enums.ParkNoticeTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 园区通知控制开关
 *
 * @author mckaywu
 * @date 2019-11-20 10:37:43
 */
@Service
public class SmtNoticeSwitchServiceImpl extends ServiceImpl<SmtNoticeSwitchMapper, SmtNoticeSwitch> implements SmtNoticeSwitchService {

	@Override
	public SmtNoticeSwitch getSwitchByCode(Integer parkId, String switchCode) {
		QueryWrapper<SmtNoticeSwitch> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(SmtNoticeSwitch::getParkId, parkId)
				.eq(SmtNoticeSwitch::getSwitchCode, switchCode);
		return baseMapper.selectOne(queryWrapper);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean batchSave(Integer parkId, List<SmtNoticeSwitch> switchList) {
		if (CollectionUtils.isNotEmpty(switchList)) {
			for (SmtNoticeSwitch tempSwitch : switchList) {
				if (Objects.nonNull(this.getSwitchByCode(parkId, tempSwitch.getSwitchCode()))) {
					this.updateById(tempSwitch);
				} else {
					tempSwitch.setParkId(parkId);
					tempSwitch.setCreateTime(LocalDateTime.now());
					this.save(tempSwitch);
				}
			}
			return Boolean.TRUE;
		}
/**/
		return Boolean.FALSE;
	}

	@Override
	public List<SmtNoticeSwitch> listInitSwitch(Integer parkId) {
		List<SmtNoticeSwitch> dbSwitchList = new ArrayList<>();

		List<Map<String, String>> menuList = ParkNoticeTypeEnum.list();
		String tempEnumCode;
		String tempEnumDesc;
		SmtNoticeSwitch tempSmtNoticeSwitch;
		for (Map<String, String> tempEnmuMap : menuList) {
			tempEnumCode = tempEnmuMap.get("code");
			tempEnumDesc = tempEnmuMap.get("desc");
			//查询数据库是否保存了该开关
			tempSmtNoticeSwitch = this.getSwitchByCode(parkId, tempEnumCode);
			if (Objects.nonNull(tempSmtNoticeSwitch) && tempEnumCode.equals(tempSmtNoticeSwitch.getSwitchCode())) {
				tempSmtNoticeSwitch.setSwitchName(tempEnumDesc);
			} else {
				tempSmtNoticeSwitch = new SmtNoticeSwitch(tempEnumCode,tempEnumDesc);
			}

			dbSwitchList.add(tempSmtNoticeSwitch);
		}

		return dbSwitchList;
	}

}
