package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwAcardlostAll;
import com.tce.smart.ehrview.core.entity.OvwYsCallOwanceDetails;
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
public interface OvwYsCallOwanceDetailsMapper extends BaseMapper<OvwYsCallOwanceDetails> {

	OvwYsCallOwanceDetails getByBadge(@Param("badge") String badge);

}
