package com.tce.smart.businesstrip.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.businesstrip.core.entity.FormTableMain182Dt1;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 11:50
 */
@Mapper
public interface FormTableMain182Dt1Mapper extends BaseMapper<FormTableMain182Dt1> {

	/**
	 * 通过mainId获取数组
	 * @param mainId
	 * @return
	 */
	List<FormTableMain182Dt1> getByMainId(@Param("mainId") Integer mainId);

	/**
	 * 修改返厂时间
	 * @param dt1List
	 * @return
	 */
	Boolean updateFcsj(@Param("dt1List") List<FormTableMain182Dt1> dt1List);
}
