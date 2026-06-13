package com.tce.smart.businesstrip.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.businesstrip.core.entity.FormTableMain182;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 11:50
 */
@Mapper
public interface FormTableMain182Mapper extends BaseMapper<FormTableMain182> {

	/**
	 * 获取对象通过requestId
	 * @param requestId
	 * @return
	 */
	FormTableMain182 getByRequestId(@Param("requestId") String requestId);
}
