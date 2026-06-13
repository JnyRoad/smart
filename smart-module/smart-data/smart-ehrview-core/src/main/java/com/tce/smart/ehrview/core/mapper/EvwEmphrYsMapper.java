package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.ehrview.core.entity.EvwEmphrYs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public interface EvwEmphrYsMapper extends BaseMapper<EvwEmphrYs> {

	IPage<EvwEmphrYs> getPage(Page page, @Param("compId") List<Integer> compId);


}
