package com.tce.smart.admin.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.admin.api.entity.SysUserPark;
import org.apache.ibatis.annotations.Param;

/***
 * description: 用户园区表Mapper <br>
 * date: 2019/11/20 17:23 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public interface SysUserParkMapper extends BaseMapper<SysUserPark> {

	/**
	 * 根据用户Id删除该用户的园区关系
	 *
	 * @param userId 用户ID
	 * @return boolean
	 */
	Boolean deleteByUserId(@Param("userId") Integer userId);
}
