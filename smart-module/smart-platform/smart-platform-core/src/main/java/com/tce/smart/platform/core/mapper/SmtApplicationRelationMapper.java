package com.tce.smart.platform.core.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtApplicationRelation;

/**
 * 应聘者人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
public interface SmtApplicationRelationMapper extends BaseMapper<SmtApplicationRelation> {

    List<SmtApplicationRelation> selectByApplicationId(String applicationId);
}
