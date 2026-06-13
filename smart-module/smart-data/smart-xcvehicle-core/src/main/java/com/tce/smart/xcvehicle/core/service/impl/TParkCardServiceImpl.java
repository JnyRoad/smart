package com.tce.smart.xcvehicle.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.xcvehicle.core.dto.CarTypeEnum;
import com.tce.smart.xcvehicle.core.dto.TParkCardAddDTO;
import com.tce.smart.xcvehicle.core.entity.TParkCard;
import com.tce.smart.xcvehicle.core.mapper.TParkCardMapper;
import com.tce.smart.xcvehicle.core.service.TParkCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class TParkCardServiceImpl extends ServiceImpl<TParkCardMapper, TParkCard> implements TParkCardService {

	@Transactional
	@Override
	public Boolean addParkCard(TParkCardAddDTO tParkCardAddDTO) {
		// 获取当前最大ID
		Integer maxCId = this.baseMapper.getMaxCId();
		//插入车牌信息
		this.baseMapper.addParkCard(tParkCardAddDTO,maxCId+1);
		//查询用户信息
		String user = this.baseMapper.queryUser(tParkCardAddDTO.getUserName());
		if(StringUtils.hasLength(user)){
			//更新用户信息
			this.baseMapper.updateUser(tParkCardAddDTO.getPhone(),tParkCardAddDTO.getUserName());
		}
		//更新车辆类型
		this.baseMapper.updateGateIo(tParkCardAddDTO.getCarType(), CarTypeEnum.desc(tParkCardAddDTO.getCarType()),tParkCardAddDTO.getPlat());

		//设置车辆有效期
		this.baseMapper.addValidDate(tParkCardAddDTO.getPlat(),tParkCardAddDTO.getStartDate(),tParkCardAddDTO.getEndDate());

		//插入日志
		this.baseMapper.addCardMLog(tParkCardAddDTO.getPlat());
		return Boolean.TRUE;
	}

	@Transactional
	@Override
	public Boolean deleteParkCard(String cardNo) {
		this.baseMapper.deleteParkCard(cardNo);
		this.baseMapper.deleteCardValidDateRange(cardNo);
		return Boolean.TRUE;
	}
}
