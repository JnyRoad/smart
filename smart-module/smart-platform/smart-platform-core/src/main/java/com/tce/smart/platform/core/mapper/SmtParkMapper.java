package com.tce.smart.platform.core.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtPark;

/**
 * 园区表
 *
 * @author 齐佩
 * @date 2019-04-13 13:48:12
 */
public interface SmtParkMapper extends BaseMapper<SmtPark> {

	IPage<SmtPark> getDistanceList(Page<SmtPark> page, @Param("condition")SmtPark smtPark );

}
