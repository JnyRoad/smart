package com.tce.smart.admin.mapper;

import java.util.Date;

import  org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.admin.api.entity.SysMoveDataTask;

/**
 * <p>
 * 数据转移务表配置Mapper 接口
 * </p>
 *
 */
public interface SysMoveDataTaskMapper extends BaseMapper<SysMoveDataTask> {

	/**
	 * 迁移数据到历史表
	 *
	 * @param srcTable   源表
	 * @param destTable  目标表
	 * @param dateColumn 时间列名
	 * @param endDate    转移此日期前的数据
	 */
	void moveData(@Param("srcTable") String srcTable, @Param("destTable") String destTable,
			@Param("dateColumn") String dateColumn, @Param("endDate") Date endDate);

	/**
	 * 删除源表数据
	 *
	 * @param tableName  源表
	 * @param dateColumn 时间列名
	 * @param endDate    转移此日期前的数据
	 */
	int deleteData(@Param("tableName") String tableName, @Param("dateColumn") String dateColumn,
			@Param("endDate") Date endDate);

}
