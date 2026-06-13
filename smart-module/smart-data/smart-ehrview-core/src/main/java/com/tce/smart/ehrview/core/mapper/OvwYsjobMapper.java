package com.tce.smart.ehrview.core.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.OvwYsjob;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public interface OvwYsjobMapper extends BaseMapper<OvwYsjob> {

	Integer getByCompId(Integer compId);

	List<OvwYsjob> getListByCompId(Integer compId);

	List<OvwYsjob> getJchenList();

}
