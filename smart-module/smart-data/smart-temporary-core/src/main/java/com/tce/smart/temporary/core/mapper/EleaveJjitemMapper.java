package com.tce.smart.temporary.core.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.temporary.core.entity.EleaveJjitem;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public interface EleaveJjitemMapper extends BaseMapper<EleaveJjitem> {

   int saveBatchEleaveJjitem(List<EleaveJjitem> list);
}
