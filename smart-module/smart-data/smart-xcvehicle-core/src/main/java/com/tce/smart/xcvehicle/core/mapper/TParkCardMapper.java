package com.tce.smart.xcvehicle.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.xcvehicle.core.dto.TParkCardAddDTO;
import com.tce.smart.xcvehicle.core.entity.TParkCard;
import org.apache.ibatis.annotations.Param;

/**
 *
 * @author wuling
 * @date 2021-03-11
 */
public interface TParkCardMapper extends BaseMapper<TParkCard> {
	Integer getMaxCId();

	Integer addParkCard(@Param("param")TParkCardAddDTO tParkCardAddDTO,@Param("maxCId")Integer maxCId);

	String queryUser(@Param("userName")String userName);

	Integer updateUser(@Param("phone")String phone,@Param("userName")String userName);

	Integer updateGateIo(@Param("carType")Integer carType,@Param("carTypedesc")String carTypedesc,@Param("plat")String plat);

	Integer addValidDate(@Param("plat")String plat,@Param("startDate")String startDate,@Param("endDate")String endDate);

	Integer addCardMLog(@Param("plat")String plat);

	Integer deleteParkCard(@Param("cardNo")String cardNo);

	Integer deleteCardValidDateRange(@Param("cardNo")String cardNo);
}
