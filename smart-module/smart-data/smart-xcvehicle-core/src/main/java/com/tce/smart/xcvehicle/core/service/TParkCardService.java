package com.tce.smart.xcvehicle.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.xcvehicle.core.dto.TParkCardAddDTO;
import com.tce.smart.xcvehicle.core.entity.TParkCard;

import java.util.Date;
import java.util.Map;


public interface TParkCardService extends IService<TParkCard> {
	Boolean addParkCard(TParkCardAddDTO tParkCardAddDTO);

	Boolean deleteParkCard(String cardNo);
}
