package com.tce.smart.businesstrip.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.businesstrip.core.entity.FormTableMain182Dt2;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 11:50
 */
@Mapper
public interface FormTableMain182Dt2Mapper extends BaseMapper<FormTableMain182Dt2> {

	/**
	 * 通过mainId获取数组
	 * @param mainId
	 * @return
	 */
	List<FormTableMain182Dt2> getByMainId(@Param("mainId") Integer mainId);

	/**
	 * 修改返厂时间
	 * @param dt2List
	 * @return
	 */
	Boolean updateFcsj(@Param("dt2List") List<FormTableMain182Dt2> dt2List);
}
